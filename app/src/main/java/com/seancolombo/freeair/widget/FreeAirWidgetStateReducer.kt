package com.seancolombo.freeair.widget

import com.seancolombo.freeair.airquality.AirQualityReading
import com.seancolombo.freeair.airquality.Pm25AqiCalculator
import com.seancolombo.freeair.airquality.purpleair.PurpleAirMapUrlBuilder

/**
 * Combines a fetch attempt with whatever was last cached, deciding both what the widget should
 * render and what (if anything) should be persisted. On a failed fetch, falls back to the last
 * known reading rather than going blank -- its own "last updated" time makes the staleness
 * visible, so a transient network blip doesn't hide data the user can still act on.
 */
object FreeAirWidgetStateReducer {
    data class Outcome(val state: FreeAirWidgetState, val cacheToPersist: CachedWidgetReading?)

    fun reduce(fetchResult: Result<AirQualityReading>, cachedReading: CachedWidgetReading?): Outcome =
        fetchResult.fold(
            onSuccess = { reading ->
                val aqi = Pm25AqiCalculator.calculate(reading.pm25)
                val sensorName = reading.sensorName.ifBlank { "Sensor ${reading.sensorId}" }
                val mapUrl = if (reading.latitude != null && reading.longitude != null) {
                    PurpleAirMapUrlBuilder.build(reading.sensorId, reading.latitude, reading.longitude)
                } else {
                    null
                }
                val newCache = CachedWidgetReading(
                    sensorName = sensorName,
                    pm25Aqi = aqi,
                    lastUpdatedEpochSeconds = reading.lastUpdated.epochSecond,
                    mapUrl = mapUrl,
                )
                Outcome(newCache.toLoadedState(), newCache)
            },
            onFailure = { error ->
                if (cachedReading != null) {
                    Outcome(cachedReading.toLoadedState(), cachedReading)
                } else {
                    Outcome(FreeAirWidgetState.Error(error.message ?: "Unable to load sensor data"), null)
                }
            },
        )
}
