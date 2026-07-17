package com.seancolombo.freeair.airquality

/**
 * A source of air quality readings for a single sensor. Widget/UI/WorkManager code should
 * depend on this interface rather than a specific monitor network's client, so a different
 * supplier could be swapped in later without touching them.
 */
interface AirQualityProvider {
    suspend fun fetchReading(config: AirQualitySensorConfig): Result<AirQualityReading>
}
