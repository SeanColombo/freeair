package com.seancolombo.freeair.airquality.purpleair

/**
 * Parses the sensor ID out of the "Widget Code" HTML snippet that PurpleAir's website gives
 * users to embed a live sensor widget elsewhere. This will eventually let a user configure a
 * sensor by pasting that snippet instead of having to know their sensor's numeric ID.
 */
object PurpleAirWidgetCodeParser {
    private val widgetSensorIndexRegex = Regex("""PurpleAirWidget_(\d+)_""")

    fun parseSensorIndex(widgetHtml: String): Long? {
        return widgetSensorIndexRegex
            .find(widgetHtml)
            ?.groupValues
            ?.get(1)
            ?.toLongOrNull()
    }
}
