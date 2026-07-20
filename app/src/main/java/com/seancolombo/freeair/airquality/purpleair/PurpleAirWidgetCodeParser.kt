package com.seancolombo.freeair.airquality.purpleair

/**
 * Parses the sensor ID out of things a user is likely to paste into the sensor ID field instead
 * of the plain number: PurpleAir's "Widget Code" HTML snippet
 * (`id='PurpleAirWidget_183609_module_...'`), that snippet's script URL alone
 * (`...&container=PurpleAirWidget_183609_module_...'`), or just the ID with the underscores that
 * surrounded it in either of those (`_183609_`). All three share the same `_<digits>_` shape at
 * the ID itself, so one pattern covers them -- and since the ID always appears before any other
 * underscore-wrapped number in PurpleAir's own markup (e.g. the widget's `average_10_...`
 * setting), taking the first match is enough to avoid picking up the wrong one.
 */
object PurpleAirWidgetCodeParser {
    private val underscoreWrappedNumberRegex = Regex("""_(\d+)_""")

    fun parseSensorIndex(input: String): Long? {
        return underscoreWrappedNumberRegex
            .find(input)
            ?.groupValues
            ?.get(1)
            ?.toLongOrNull()
    }
}
