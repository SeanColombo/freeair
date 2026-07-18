package com.seancolombo.freeair.widget

import com.seancolombo.freeair.airquality.AirQualityReading
import com.seancolombo.freeair.airquality.Pm25AqiCalculator
import com.seancolombo.freeair.airquality.purpleair.PurpleAirMapUrlBuilder

/**
 * Combines a fetch attempt with whatever was last cached, deciding both what the widget should
 * render and what (if anything) should be persisted. On a failed fetch, falls back to the last
 * known reading rather than going blank -- its own "last updated" time makes the staleness
 * visible, so a transient network blip doesn't hide data the user can still act on. Callers must
 * only pass a [cachedReading] that's already confirmed to be for the same sensor as [sensorId] --
 * see [CachedWidgetReading] -- this reducer doesn't re-check, so a stale reading from a
 * previously-configured sensor doesn't get misrepresented as current data for a new one.
 */
object FreeAirWidgetStateReducer {
    data class Outcome(
        val state: FreeAirWidgetState,
        val cacheToPersist: CachedWidgetReading?,
        // Only ever set alongside an Error state with no cached reading to fall back to -- see
        // CachedWidgetError. Lets the in-app preview (which never fetches on its own) show the
        // same failure the widget hit, instead of being stuck on "Loading" forever.
        val errorToPersist: CachedWidgetError? = null,
    )

    fun reduce(
        fetchResult: Result<AirQualityReading>,
        cachedReading: CachedWidgetReading?,
        sensorId: String,
        appWidgetId: Int,
    ): Outcome =
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
                    sensorId = sensorId,
                    sensorName = sensorName,
                    pm25Aqi = aqi,
                    lastUpdatedEpochSeconds = reading.lastUpdated.epochSecond,
                    mapUrl = mapUrl,
                    isIndoor = reading.isIndoor,
                )
                Outcome(newCache.toLoadedState(), newCache)
            },
            onFailure = { error ->
                if (cachedReading != null) {
                    Outcome(cachedReading.toLoadedState(), cachedReading)
                } else {
                    val message = error.message ?: "Unable to load sensor data"
                    Outcome(
                        FreeAirWidgetState.Error(message, sensorId, appWidgetId),
                        cacheToPersist = null,
                        errorToPersist = CachedWidgetError(message, sensorId),
                    )
                }
            },
        )
}
