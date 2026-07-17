package com.seancolombo.freeair.airquality

import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Converts a raw PM2.5 concentration (µg/m3) into a US EPA PM2.5 AQI value, using the
 * breakpoints EPA revised in 2024. This is the "PM2.5 AQI" PurpleAir's map shows by default,
 * not the raw µg/m3 concentration.
 */
object Pm25AqiCalculator {
    private data class Breakpoint(
        val concentrationLow: Double,
        val concentrationHigh: Double,
        val aqiLow: Int,
        val aqiHigh: Int,
    )

    // Concentrations above the top breakpoint are extrapolated using its slope, matching how
    // EPA's own reference tables continue the scale past 500 for extreme events.
    private val breakpoints = listOf(
        Breakpoint(0.0, 9.0, 0, 50),
        Breakpoint(9.1, 35.4, 51, 100),
        Breakpoint(35.5, 55.4, 101, 150),
        Breakpoint(55.5, 125.4, 151, 200),
        Breakpoint(125.5, 225.4, 201, 300),
        Breakpoint(225.5, 325.4, 301, 500),
    )

    // Truncating pm25*10 with floor() before dividing back down is how EPA's own formula
    // truncates to one decimal place, but binary floating point can put a value like 9.1
    // fractionally below its literal (e.g. 90.999999999) -- nudge past that before flooring.
    private const val TRUNCATION_EPSILON = 1e-9

    fun calculate(pm25: Double): Int {
        val truncated = floor(pm25.coerceAtLeast(0.0) * 10.0 + TRUNCATION_EPSILON) / 10.0
        val breakpoint = breakpoints.last { truncated >= it.concentrationLow }
        val slope = (breakpoint.aqiHigh - breakpoint.aqiLow) /
            (breakpoint.concentrationHigh - breakpoint.concentrationLow)
        return (slope * (truncated - breakpoint.concentrationLow) + breakpoint.aqiLow).roundToInt()
    }
}
