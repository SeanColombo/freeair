package com.seancolombo.freeair.widget

import com.seancolombo.freeair.airquality.AirQualityReading
import com.seancolombo.freeair.airquality.AqiCategory
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FreeAirWidgetStateReducerTest {
    @Test
    fun `a successful reading maps to Loaded and produces a cache entry to persist`() {
        val reading = AirQualityReading(
            sensorId = "12345",
            sensorName = "Backyard Sensor",
            pm25 = 20.0,
            temperatureFahrenheit = 72.0,
            humidityPercent = 45,
            lastUpdated = Instant.ofEpochSecond(1_000),
        )

        val outcome = FreeAirWidgetStateReducer.reduce(Result.success(reading), cachedReading = null)

        assertEquals(
            FreeAirWidgetState.Loaded(
                sensorName = "Backyard Sensor",
                pm25Aqi = 71,
                category = AqiCategory.MODERATE,
                lastUpdated = Instant.ofEpochSecond(1_000),
            ),
            outcome.state,
        )
        assertEquals(CachedWidgetReading("Backyard Sensor", 71, 1_000), outcome.cacheToPersist)
    }

    @Test
    fun `a blank sensor name falls back to the sensor id`() {
        val reading = AirQualityReading(
            sensorId = "12345",
            sensorName = "",
            pm25 = 0.0,
            temperatureFahrenheit = null,
            humidityPercent = null,
            lastUpdated = Instant.EPOCH,
        )

        val outcome = FreeAirWidgetStateReducer.reduce(Result.success(reading), cachedReading = null)

        assertEquals("Sensor 12345", (outcome.state as FreeAirWidgetState.Loaded).sensorName)
    }

    @Test
    fun `a failed fetch with no cache maps to Error with the failure message`() {
        val outcome = FreeAirWidgetStateReducer.reduce(
            Result.failure(RuntimeException("network down")),
            cachedReading = null,
        )

        assertEquals(FreeAirWidgetState.Error("network down"), outcome.state)
        assertNull(outcome.cacheToPersist)
    }

    @Test
    fun `a failed fetch with a cache falls back to the last known reading instead of erroring`() {
        val cached = CachedWidgetReading(sensorName = "Backyard Sensor", pm25Aqi = 42, lastUpdatedEpochSeconds = 500)

        val outcome = FreeAirWidgetStateReducer.reduce(
            Result.failure(RuntimeException("network down")),
            cachedReading = cached,
        )

        assertEquals(
            FreeAirWidgetState.Loaded(
                sensorName = "Backyard Sensor",
                pm25Aqi = 42,
                category = AqiCategory.forAqi(42),
                lastUpdated = Instant.ofEpochSecond(500),
            ),
            outcome.state,
        )
        // Unchanged -- we didn't get fresh data, so there's nothing new to persist.
        assertEquals(cached, outcome.cacheToPersist)
    }
}
