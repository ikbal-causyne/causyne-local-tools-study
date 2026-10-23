package com.vanexa.supercalculator.domain.model

enum class UnitCategory {
    WEIGHT, LENGTH, TEMPERATURE, VOLUME, AREA, SPEED, TIME, DATA
}

enum class WeightUnit(val symbol: String, val toKg: Double) {
    KG("kg", 1.0),
    G("g", 0.001),
    MG("mg", 0.000001),
    LB("lb", 0.453592),
    OZ("oz", 0.0283495),
    TON("ton", 1000.0)
}

enum class LengthUnit(val symbol: String, val toMeter: Double) {
    M("m", 1.0),
    CM("cm", 0.01),
    MM("mm", 0.001),
    KM("km", 1000.0),
    FT("ft", 0.3048),
    INCH("inch", 0.0254),
    MILE("mile", 1609.34),
    YARD("yard", 0.9144)
}

enum class TemperatureUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F"),
    KELVIN("K")
}

enum class VolumeUnit(val symbol: String, val toLiter: Double) {
    L("L", 1.0),
    ML("mL", 0.001),
    GALLON("gal", 3.78541),
    CUP("cup", 0.236588),
    FL_OZ("fl oz", 0.0295735)
}

enum class AreaUnit(val symbol: String, val toSqMeter: Double) {
    SQ_M("m²", 1.0),
    SQ_KM("km²", 1000000.0),
    SQ_FT("ft²", 0.092903),
    ACRE("acre", 4046.86),
    HECTARE("hectare", 10000.0)
}

enum class SpeedUnit(val symbol: String, val toMps: Double) {
    MPS("m/s", 1.0),
    KMH("km/h", 0.277778),
    MPH("mph", 0.44704),
    KNOTS("knots", 0.514444)
}

enum class TimeUnit(val symbol: String, val toSeconds: Double) {
    SECOND("s", 1.0),
    MINUTE("min", 60.0),
    HOUR("h", 3600.0),
    DAY("day", 86400.0),
    WEEK("week", 604800.0),
    MONTH("month", 2592000.0),
    YEAR("year", 31536000.0)
}

enum class DataUnit(val symbol: String, val toBytes: Double) {
    BIT("bit", 0.125),
    BYTE("B", 1.0),
    KB("KB", 1024.0),
    MB("MB", 1048576.0),
    GB("GB", 1073741824.0),
    TB("TB", 1099511627776.0)
}

