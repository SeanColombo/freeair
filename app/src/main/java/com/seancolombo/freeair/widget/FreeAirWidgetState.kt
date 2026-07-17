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
