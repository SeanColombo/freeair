package com.seancolombo.freeair.widget

import com.seancolombo.freeair.airquality.AirQualityReading
import com.seancolombo.freeair.airquality.AqiCategory
import com.seancolombo.freeair.airquality.Pm25AqiCalculator

/** What the widget should render, independent of Glance/Android so it's plain-JVM testable. */
sealed class FreeAirWidgetState {
    data object Loading : FreeAirWidgetState()

    data class Loaded(
        val sensorName: String,
        val pm25Aqi: Int,
        val category: AqiCategory,
    ) : FreeAirWidgetState()

    data class Error(val message: String) : FreeAirWidgetState()
}

/** Maps a sensor fetch result to widget state, keeping this logic out of FreeAirWidget itself. */
fun Result<AirQualityReading>.toWidgetState(): FreeAirWidgetState = fold(
    onSuccess = { reading ->
        val aqi = Pm25AqiCalculator.calculate(reading.pm25)
        FreeAirWidgetState.Loaded(
            sensorName = reading.sensorName.ifBlank { "Sensor ${reading.sensorId}" },
            pm25Aqi = aqi,
            category = AqiCategory.forAqi(aqi),
        )
    },
    onFailure = { error ->
        FreeAirWidgetState.Error(error.message ?: "Unable to load sensor data")
    },
)
