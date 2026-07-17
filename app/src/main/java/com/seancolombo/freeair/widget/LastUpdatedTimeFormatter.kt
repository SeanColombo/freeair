package com.seancolombo.freeair.widget

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Formats an absolute local time (e.g. "2:14 PM") for the widget's last-updated label.
 * Deliberately absolute, not relative ("5m ago") -- Glance only re-renders on an actual
 * widget update, so a relative label would silently go stale and mislead between refreshes.
 */
object LastUpdatedTimeFormatter {
    fun format(
        instant: Instant,
        zoneId: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault(),
    ): String {
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
        return instant.atZone(zoneId).format(formatter)
    }
}
