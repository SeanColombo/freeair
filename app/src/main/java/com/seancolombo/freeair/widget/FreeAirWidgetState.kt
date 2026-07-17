package com.seancolombo.freeair.widget

import com.seancolombo.freeair.airquality.AqiCategory
import java.time.Instant

/** What the widget should render, independent of Glance/Android so it's plain-JVM testable. */
sealed class FreeAirWidgetState {
    data object Loading : FreeAirWidgetState()

    data class Loaded(
        val sensorName: String,
        val pm25Aqi: Int,
        val category: AqiCategory,
        val lastUpdated: Instant,
        val mapUrl: String? = null,
    ) : FreeAirWidgetState()

    data class Error(val message: String) : FreeAirWidgetState()
}

/** Shared by the reducer (cache fallback) and the in-app preview (read-only, no live fetch). */
internal fun CachedWidgetReading.toLoadedState(): FreeAirWidgetState.Loaded = FreeAirWidgetState.Loaded(
    sensorName = sensorName,
    pm25Aqi = pm25Aqi,
    category = AqiCategory.forAqi(pm25Aqi),
    lastUpdated = Instant.ofEpochSecond(lastUpdatedEpochSeconds),
    mapUrl = mapUrl,
)
