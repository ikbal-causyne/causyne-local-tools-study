package com.vanexa.supercalculator.domain.usecase

import com.vanexa.supercalculator.domain.model.*

class UnitConverterUseCase {
    
    fun convertWeight(value: Double, from: WeightUnit, to: WeightUnit): Double {
        val toKg = value * from.toKg
        return toKg / to.toKg
    }
    
    fun convertLength(value: Double, from: LengthUnit, to: LengthUnit): Double {
        val toMeter = value * from.toMeter
        return toMeter / to.toMeter
    }
    
    fun convertTemperature(value: Double, from: TemperatureUnit, to: TemperatureUnit): Double {
        // Convert to Celsius first
        val celsius = when (from) {
            TemperatureUnit.CELSIUS -> value
            TemperatureUnit.FAHRENHEIT -> (value - 32) * 5 / 9
            TemperatureUnit.KELVIN -> value - 273.15
        }
        
        // Convert from Celsius to target
        return when (to) {
            TemperatureUnit.CELSIUS -> celsius
            TemperatureUnit.FAHRENHEIT -> celsius * 9 / 5 + 32
            TemperatureUnit.KELVIN -> celsius + 273.15
        }
    }
    
    fun convertVolume(value: Double, from: VolumeUnit, to: VolumeUnit): Double {
        val toLiter = value * from.toLiter
        return toLiter / to.toLiter
    }
    
    fun convertArea(value: Double, from: AreaUnit, to: AreaUnit): Double {
        val toSqMeter = value * from.toSqMeter
        return toSqMeter / to.toSqMeter
    }
    
    fun convertSpeed(value: Double, from: SpeedUnit, to: SpeedUnit): Double {
        val toMps = value * from.toMps
        return toMps / to.toMps
    }
    
    fun convertTime(value: Double, from: TimeUnit, to: TimeUnit): Double {
        val toSeconds = value * from.toSeconds
        return toSeconds / to.toSeconds
    }
    
    fun convertData(value: Double, from: DataUnit, to: DataUnit): Double {
        val toBytes = value * from.toBytes
        return toBytes / to.toBytes
    }
}

