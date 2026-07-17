package com.seancolombo.freeair.widget

/**
 * The last successfully rendered reading, persisted across process death so a transient fetch
 * failure can fall back to "last known" instead of going blank. Category isn't stored here --
 * it's always recomputed from [pm25Aqi] so a color/threshold change can't leave stale data
 * inconsistent with the current mapping.
 */
data class CachedWidgetReading(
    val sensorName: String,
    val pm25Aqi: Int,
    val lastUpdatedEpochSeconds: Long,
    // Nullable both because the sensor might not report a location, and so cached data written
    // by an older version of the app (before this field existed) still loads instead of being
    // discarded entirely.
    val mapUrl: String? = null,
)
