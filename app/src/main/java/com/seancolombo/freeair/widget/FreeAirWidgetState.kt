package com.seancolombo.freeair.widget

import com.seancolombo.freeair.airquality.AqiCategory
import java.time.Instant

/** What the widget should render, independent of Glance/Android so it's plain-JVM testable. */
sealed class FreeAirWidgetState {
    data object Loading : FreeAirWidgetState()

    /** No sensor saved for this widget instance yet -- rendered as a tap-to-configure state. */
    data class NeedsSetup(val appWidgetId: Int) : FreeAirWidgetState()

    data class Loaded(
        val sensorName: String,
        val pm25Aqi: Int,
        val category: AqiCategory,
        val lastUpdated: Instant,
        val mapUrl: String? = null,
        val isIndoor: Boolean = false,
    ) : FreeAirWidgetState()

    // message is the raw, unformatted error (e.g. an HTTP client's exception message) -- kept
    // verbatim so it's available for debugging and so a future app update to
    // WidgetErrorMessageFormatter can retroactively render already-cached errors better, without
    // needing to have re-fetched. sensorId lets the formatter build messages like "Sensor ID
    // {id} not found" without the view layer needing separate access to the widget's config.
    // appWidgetId lets an error be tapped straight through to config, the same as NeedsSetup --
    // an error is most often a bad sensor ID, so that's the most useful thing to fix from here.
    data class Error(val message: String, val sensorId: String, val appWidgetId: Int) : FreeAirWidgetState()
}

/** Shared by the reducer (cache fallback) and the in-app preview (read-only, no live fetch). */
internal fun CachedWidgetReading.toLoadedState(): FreeAirWidgetState.Loaded = FreeAirWidgetState.Loaded(
    sensorName = sensorName,
    pm25Aqi = pm25Aqi,
    category = AqiCategory.forAqi(pm25Aqi),
    lastUpdated = Instant.ofEpochSecond(lastUpdatedEpochSeconds),
    mapUrl = mapUrl,
    isIndoor = isIndoor,
)
