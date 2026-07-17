package com.seancolombo.freeair.airquality

/**
 * US EPA Air Quality Index categories for PM2.5. Thresholds and colors match the scale
 * PurpleAir's map uses by default (which is the EPA scale, revised in 2024 -- see
 * [Pm25AqiCalculator]). Content colors follow AirNow's own convention: black text on the
 * three brighter categories, white text on the three darker ones.
 */
enum class AqiCategory(
    val label: String,
    val backgroundColor: RgbColor,
    val contentColor: RgbColor,
) {
    GOOD("Good", RgbColor(0x00, 0xE4, 0x00), RgbColor(0x00, 0x00, 0x00)),
    MODERATE("Moderate", RgbColor(0xFF, 0xFF, 0x00), RgbColor(0x00, 0x00, 0x00)),
    UNHEALTHY_FOR_SENSITIVE_GROUPS(
        "Unhealthy for Sensitive Groups",
        RgbColor(0xFF, 0x7E, 0x00),
        RgbColor(0x00, 0x00, 0x00),
    ),
    UNHEALTHY("Unhealthy", RgbColor(0xFF, 0x00, 0x00), RgbColor(0xFF, 0xFF, 0xFF)),
    VERY_UNHEALTHY("Very Unhealthy", RgbColor(0x8F, 0x3F, 0x97), RgbColor(0xFF, 0xFF, 0xFF)),
    HAZARDOUS("Hazardous", RgbColor(0x7E, 0x00, 0x23), RgbColor(0xFF, 0xFF, 0xFF)),
    ;

    companion object {
        fun forAqi(aqi: Int): AqiCategory = when {
            aqi <= 50 -> GOOD
            aqi <= 100 -> MODERATE
            aqi <= 150 -> UNHEALTHY_FOR_SENSITIVE_GROUPS
            aqi <= 200 -> UNHEALTHY
            aqi <= 300 -> VERY_UNHEALTHY
            else -> HAZARDOUS
        }
    }
}
