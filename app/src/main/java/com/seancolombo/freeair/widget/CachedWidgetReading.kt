package com.seancolombo.freeair.widget

/**
 * The last successfully rendered reading, persisted across process death so a transient fetch
 * failure can fall back to "last known" instead of going blank. Category isn't stored here --
 * it's always recomputed from [pm25Aqi] so a color/threshold change can't leave stale data
 * inconsistent with the current mapping.
 *
 * sensorId is whichever sensor produced this reading. If the widget's current sensor config no
 * longer matches it (the user has since saved a different ID), this reading is stale and
 * shouldn't be used as a "last known good" fallback for the new one -- see [CachedWidgetError]
 * for the same reasoning applied to failures.
 */
data class CachedWidgetReading(
    val sensorId: String,
    val sensorName: String,
    val pm25Aqi: Int,
    val lastUpdatedEpochSeconds: Long,
    // Nullable both because the sensor might not report a location, and so cached data written
    // by an older version of the app (before this field existed) still loads instead of being
    // discarded entirely.
    val mapUrl: String? = null,
    // Defaults to false (outside) so cached data written before this field existed still loads.
    val isIndoor: Boolean = false,
)
