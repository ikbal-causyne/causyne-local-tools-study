package com.vanexa.supercalculator.presentation.converter

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanexa.supercalculator.domain.model.*
import com.vanexa.supercalculator.domain.usecase.UnitConverterUseCase
import com.vanexa.supercalculator.presentation.common.TextToSpeechHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnitConverterViewModel constructor(
    private val converterUseCase: UnitConverterUseCase,
    private val ttsHelper: TextToSpeechHelper
) : ViewModel() {
    
    private val _selectedCategory = MutableStateFlow(UnitCategory.WEIGHT)
    val selectedCategory: StateFlow<UnitCategory> = _selectedCategory.asStateFlow()
    
    private val _inputValue = MutableStateFlow("1.0")
    val inputValue: StateFlow<String> = _inputValue.asStateFlow()
    
    private val _resultValue = MutableStateFlow("0.0")
    val resultValue: StateFlow<String> = _resultValue.asStateFlow()
    
    private val _fromUnit = MutableStateFlow<Any?>(WeightUnit.KG)
    val fromUnit: StateFlow<Any?> = _fromUnit.asStateFlow()
    
    private val _toUnit = MutableStateFlow<Any?>(WeightUnit.LB)
    val toUnit: StateFlow<Any?> = _toUnit.asStateFlow()
    
    private val _voiceEnabled = MutableStateFlow(false)  // ✅ Default OFF for user comfort
    val voiceEnabled: StateFlow<Boolean> = _voiceEnabled.asStateFlow()
    
    fun setCategory(category: UnitCategory) {
        _selectedCategory.value = category
        // Reset units based on category
        when (category) {
            UnitCategory.WEIGHT -> {
                _fromUnit.value = WeightUnit.KG
                _toUnit.value = WeightUnit.LB
            }
            UnitCategory.LENGTH -> {
                _fromUnit.value = LengthUnit.M
                _toUnit.value = LengthUnit.FT
            }
            UnitCategory.TEMPERATURE -> {
                _fromUnit.value = TemperatureUnit.CELSIUS
                _toUnit.value = TemperatureUnit.FAHRENHEIT
            }
            UnitCategory.VOLUME -> {
                _fromUnit.value = VolumeUnit.L
                _toUnit.value = VolumeUnit.GALLON
            }
            UnitCategory.AREA -> {
                _fromUnit.value = AreaUnit.SQ_M
                _toUnit.value = AreaUnit.SQ_FT
            }
            UnitCategory.SPEED -> {
                _fromUnit.value = SpeedUnit.KMH
                _toUnit.value = SpeedUnit.MPH
            }
            UnitCategory.TIME -> {
                _fromUnit.value = TimeUnit.HOUR
                _toUnit.value = TimeUnit.MINUTE
            }
            UnitCategory.DATA -> {
                _fromUnit.value = DataUnit.MB
                _toUnit.value = DataUnit.GB
            }
        }
        convert()
    }
    
    fun setInputValue(value: String) {
        _inputValue.value = value
        convert()
    }
    
    fun setFromUnit(unit: Any) {
        _fromUnit.value = unit
        convert()
    }
    
    fun setToUnit(unit: Any) {
        _toUnit.value = unit
        convert()
    }
    
    private fun convert() {
        val input = _inputValue.value.toDoubleOrNull() ?: return
        val from = _fromUnit.value
        val to = _toUnit.value
        
        if (from == null || to == null) return
        
        val result = try {
            when (_selectedCategory.value) {
                UnitCategory.WEIGHT -> {
                    if (from is WeightUnit && to is WeightUnit) {
                        converterUseCase.convertWeight(input, from, to)
                    } else {
                        _resultValue.value = "0.0"
                        return
                    }
                }
                UnitCategory.LENGTH -> {
                    if (from is LengthUnit && to is LengthUnit) {
                        converterUseCase.convertLength(input, from, to)
                    } else {
                        _resultValue.value = "0.0"
                        return
                    }
                }
                UnitCategory.TEMPERATURE -> {
                    if (from is TemperatureUnit && to is TemperatureUnit) {
                        converterUseCase.convertTemperature(input, from, to)
                    } else {
                        _resultValue.value = "0.0"
                        return
                    }
                }
                UnitCategory.VOLUME -> {
                    if (from is VolumeUnit && to is VolumeUnit) {
                        converterUseCase.convertVolume(input, from, to)
                    } else {
                        _resultValue.value = "0.0"
                        return
                    }
                }
                UnitCategory.AREA -> {
                    if (from is AreaUnit && to is AreaUnit) {
                        converterUseCase.convertArea(input, from, to)
                    } else {
                        _resultValue.value = "0.0"
                        return
                    }
                }
                UnitCategory.SPEED -> {
                    if (from is SpeedUnit && to is SpeedUnit) {
                        converterUseCase.convertSpeed(input, from, to)
                    } else {
                        _resultValue.value = "0.0"
                        return
                    }
                }
                UnitCategory.TIME -> {
                    if (from is TimeUnit && to is TimeUnit) {
                        converterUseCase.convertTime(input, from, to)
                    } else {
                        _resultValue.value = "0.0"
                        return
                    }
                }
                UnitCategory.DATA -> {
                    if (from is DataUnit && to is DataUnit) {
                        converterUseCase.convertData(input, from, to)
                    } else {
                        _resultValue.value = "0.0"
                        return
                    }
                }
            }
        } catch (e: ClassCastException) {
            Log.e("UnitConverter", "Type mismatch: ${e.message}")
            _resultValue.value = "0.0"
            return
        } catch (e: Exception) {
            Log.e("UnitConverter", "Conversion error: ${e.message}")
            _resultValue.value = "0.0"
            return
        }
        
        _resultValue.value = try {
            String.format("%.6f", result).trimEnd('0').trimEnd('.')
        } catch (e: Exception) {
            "0.0"
        }
        
        if (_voiceEnabled.value && _resultValue.value != "0.0") {
            ttsHelper.speak("${_resultValue.value}")
        }
    }
    
    fun toggleVoice() {
        _voiceEnabled.value = !_voiceEnabled.value
    }
    
    // Handle keypad input for value field (like Currency, with minus support for Temperature)
    fun onKeypadClick(key: String, cursorPosition: Int) {
        val currentValue = _inputValue.value
        when (key) {
            "C" -> {
                _inputValue.value = "0"
                convert()
                return
            }
            "⌫" -> {
                if (currentValue.length > 1) {
                    val newText = if (cursorPosition > 0 && cursorPosition <= currentValue.length) {
                        currentValue.substring(0, cursorPosition - 1) + currentValue.substring(cursorPosition)
                    } else {
                        currentValue.dropLast(1)
                    }
                    _inputValue.value = if (newText.isEmpty() || newText == "." || newText == "-") "0" else newText
                } else {
                    _inputValue.value = "0"
                }
                convert()
                return
            }
            "-" -> {
                // ✅ Minus button: Only for Temperature category
                // Allow minus only at the beginning, and only if not already present
                if (currentValue.startsWith("-")) {
                    // Remove minus if already present
                    _inputValue.value = if (currentValue.length > 1) currentValue.substring(1) else "0"
                } else {
                    // Add minus at the beginning
                    if (cursorPosition == 0) {
                        _inputValue.value = "-" + currentValue
                    } else {
                        // If cursor is not at start, move minus to beginning
                        val textWithoutMinus = currentValue.replace("-", "")
                        _inputValue.value = "-" + textWithoutMinus
                    }
                }
                convert()
            }
            "." -> {
                // Only allow one decimal point
                if (currentValue.contains(".")) {
                    return
                }
                val newValue = if (cursorPosition >= 0 && cursorPosition <= currentValue.length) {
                    currentValue.substring(0, cursorPosition) + "." + currentValue.substring(cursorPosition)
                } else {
                    currentValue + "."
                }
                _inputValue.value = newValue
                convert()
            }
            else -> {
                // Digit input (0-9)
                val newValue = if (cursorPosition >= 0 && cursorPosition <= currentValue.length) {
                    val newText = currentValue.substring(0, cursorPosition) + key + currentValue.substring(cursorPosition)
                    // Remove leading zero if adding digit after it (but keep if it's "0." or "-0.")
                    if (newText.replace("-", "").startsWith("0") && newText.replace("-", "").length > 1 && newText.replace("-", "")[1] != '.') {
                        // Preserve minus sign if present
                        val hasMinus = newText.startsWith("-")
                        val withoutMinus = newText.replace("-", "").drop(1)
                        if (hasMinus) "-$withoutMinus" else withoutMinus
                    } else {
                        newText
                    }
                } else {
                    val newText = currentValue + key
                    if (newText.replace("-", "").startsWith("0") && newText.replace("-", "").length > 1 && newText.replace("-", "")[1] != '.') {
                        // Preserve minus sign if present
                        val hasMinus = newText.startsWith("-")
                        val withoutMinus = newText.replace("-", "").drop(1)
                        if (hasMinus) "-$withoutMinus" else withoutMinus
                    } else {
                        newText
                    }
                }
                _inputValue.value = newValue
                convert()
            }
        }
    }
}

