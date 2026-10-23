package com.vanexa.supercalculator.domain.usecase

import com.vanexa.supercalculator.domain.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UnitConverterUseCaseTest {
    
    private lateinit var unitConverterUseCase: UnitConverterUseCase
    
    @Before
    fun setup() {
        unitConverterUseCase = UnitConverterUseCase()
    }
    
    @Test
    fun `test weight conversion kg to lb`() {
        val result = unitConverterUseCase.convertWeight(1.0, WeightUnit.KG, WeightUnit.LB)
        // 1 kg = 2.20462 lb
        assertEquals(2.20462, result, 0.001)
    }
    
    @Test
    fun `test weight conversion lb to kg`() {
        val result = unitConverterUseCase.convertWeight(2.20462, WeightUnit.LB, WeightUnit.KG)
        assertEquals(1.0, result, 0.001)
    }
    
    @Test
    fun `test weight conversion kg to g`() {
        val result = unitConverterUseCase.convertWeight(1.0, WeightUnit.KG, WeightUnit.G)
        assertEquals(1000.0, result, 0.001)
    }
    
    @Test
    fun `test length conversion m to ft`() {
        val result = unitConverterUseCase.convertLength(1.0, LengthUnit.M, LengthUnit.FT)
        // 1 m = 3.28084 ft
        assertEquals(3.28084, result, 0.001)
    }
    
    @Test
    fun `test length conversion km to mile`() {
        val result = unitConverterUseCase.convertLength(1.0, LengthUnit.KM, LengthUnit.MILE)
        // 1 km = 0.621371 mile
        assertEquals(0.621371, result, 0.001)
    }
    
    @Test
    fun `test temperature conversion celsius to fahrenheit`() {
        val result = unitConverterUseCase.convertTemperature(0.0, TemperatureUnit.CELSIUS, TemperatureUnit.FAHRENHEIT)
        // 0°C = 32°F
        assertEquals(32.0, result, 0.1)
    }
    
    @Test
    fun `test temperature conversion fahrenheit to celsius`() {
        val result = unitConverterUseCase.convertTemperature(32.0, TemperatureUnit.FAHRENHEIT, TemperatureUnit.CELSIUS)
        assertEquals(0.0, result, 0.1)
    }
    
    @Test
    fun `test temperature conversion celsius to kelvin`() {
        val result = unitConverterUseCase.convertTemperature(0.0, TemperatureUnit.CELSIUS, TemperatureUnit.KELVIN)
        // 0°C = 273.15K
        assertEquals(273.15, result, 0.01)
    }
    
    @Test
    fun `test temperature conversion 100 celsius to fahrenheit`() {
        val result = unitConverterUseCase.convertTemperature(100.0, TemperatureUnit.CELSIUS, TemperatureUnit.FAHRENHEIT)
        // 100°C = 212°F
        assertEquals(212.0, result, 0.1)
    }
    
    @Test
    fun `test volume conversion liter to gallon`() {
        val result = unitConverterUseCase.convertVolume(1.0, VolumeUnit.L, VolumeUnit.GALLON)
        // 1 L = 0.264172 gallon
        assertEquals(0.264172, result, 0.001)
    }
    
    @Test
    fun `test volume conversion liter to ml`() {
        val result = unitConverterUseCase.convertVolume(1.0, VolumeUnit.L, VolumeUnit.ML)
        assertEquals(1000.0, result, 0.001)
    }
    
    @Test
    fun `test area conversion sq meter to sq feet`() {
        val result = unitConverterUseCase.convertArea(1.0, AreaUnit.SQ_M, AreaUnit.SQ_FT)
        // 1 m² = 10.7639 ft²
        assertEquals(10.7639, result, 0.01)
    }
    
    @Test
    fun `test speed conversion kmh to mph`() {
        val result = unitConverterUseCase.convertSpeed(100.0, SpeedUnit.KMH, SpeedUnit.MPH)
        // 100 km/h = 62.1371 mph
        assertEquals(62.1371, result, 0.1)
    }
    
    @Test
    fun `test time conversion hour to minute`() {
        val result = unitConverterUseCase.convertTime(1.0, TimeUnit.HOUR, TimeUnit.MINUTE)
        assertEquals(60.0, result, 0.001)
    }
    
    @Test
    fun `test time conversion day to hour`() {
        val result = unitConverterUseCase.convertTime(1.0, TimeUnit.DAY, TimeUnit.HOUR)
        assertEquals(24.0, result, 0.001)
    }
    
    @Test
    fun `test data conversion mb to kb`() {
        val result = unitConverterUseCase.convertData(1.0, DataUnit.MB, DataUnit.KB)
        assertEquals(1024.0, result, 0.001)
    }
    
    @Test
    fun `test data conversion gb to mb`() {
        val result = unitConverterUseCase.convertData(1.0, DataUnit.GB, DataUnit.MB)
        assertEquals(1024.0, result, 0.001)
    }
    
    @Test
    fun `test same unit conversion returns same value`() {
        val result = unitConverterUseCase.convertWeight(5.0, WeightUnit.KG, WeightUnit.KG)
        assertEquals(5.0, result, 0.001)
    }
}

