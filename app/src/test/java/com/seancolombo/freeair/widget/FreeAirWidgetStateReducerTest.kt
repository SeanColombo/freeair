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
            latitude = 47.6062,
            longitude = -122.3321,
            isIndoor = false,
        )

        val outcome = FreeAirWidgetStateReducer.reduce(
            Result.success(reading),
            cachedReading = null,
            sensorId = "12345",
            appWidgetId = 42,
        )

        val expectedMapUrl = "https://map.purpleair.com/1/l/m/i/mAQI/a10/p2592000/cC0?select=12345#14.0/47.6062/-122.3321"
        assertEquals(
            FreeAirWidgetState.Loaded(
                sensorName = "Backyard Sensor",
                pm25Aqi = 71,
                category = AqiCategory.MODERATE,
                lastUpdated = Instant.ofEpochSecond(1_000),
                mapUrl = expectedMapUrl,
            ),
            outcome.state,
        )
        assertEquals(CachedWidgetReading("Backyard Sensor", 71, 1_000, expectedMapUrl), outcome.cacheToPersist)
    }

    @Test
    fun `a reading with no location produces a null mapUrl instead of failing`() {
        val reading = AirQualityReading(
            sensorId = "12345",
            sensorName = "Backyard Sensor",
            pm25 = 20.0,
            temperatureFahrenheit = null,
            humidityPercent = null,
            lastUpdated = Instant.EPOCH,
            latitude = null,
            longitude = null,
            isIndoor = false,
        )

        val outcome = FreeAirWidgetStateReducer.reduce(
            Result.success(reading),
            cachedReading = null,
            sensorId = "12345",
            appWidgetId = 42,
        )

        assertNull((outcome.state as FreeAirWidgetState.Loaded).mapUrl)
        assertNull(outcome.cacheToPersist?.mapUrl)
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
            latitude = null,
            longitude = null,
            isIndoor = false,
        )

        val outcome = FreeAirWidgetStateReducer.reduce(
            Result.success(reading),
            cachedReading = null,
            sensorId = "12345",
            appWidgetId = 42,
        )

        assertEquals("Sensor 12345", (outcome.state as FreeAirWidgetState.Loaded).sensorName)
    }

    @Test
    fun `an indoor sensor's reading is marked indoor in both the rendered state and the cache`() {
        val reading = AirQualityReading(
            sensorId = "12345",
            sensorName = "Living Room Sensor",
            pm25 = 20.0,
            temperatureFahrenheit = null,
            humidityPercent = null,
            lastUpdated = Instant.EPOCH,
            latitude = null,
            longitude = null,
            isIndoor = true,
        )

        val outcome = FreeAirWidgetStateReducer.reduce(
            Result.success(reading),
            cachedReading = null,
            sensorId = "12345",
            appWidgetId = 42,
        )

        assertEquals(true, (outcome.state as FreeAirWidgetState.Loaded).isIndoor)
        assertEquals(true, outcome.cacheToPersist?.isIndoor)
    }

    @Test
    fun `a failed fetch with no cache maps to Error with the raw failure message, sensor id, and widget id`() {
        val outcome = FreeAirWidgetStateReducer.reduce(
            Result.failure(RuntimeException("network down")),
            cachedReading = null,
            sensorId = "12345",
            appWidgetId = 42,
        )

        assertEquals(FreeAirWidgetState.Error("network down", sensorId = "12345", appWidgetId = 42), outcome.state)
        assertNull(outcome.cacheToPersist)
    }

    @Test
    fun `a failed fetch with no cache also produces an error to persist, for the in-app preview to share`() {
        val outcome = FreeAirWidgetStateReducer.reduce(
            Result.failure(RuntimeException("HTTP 404")),
            cachedReading = null,
            sensorId = "12345",
            appWidgetId = 42,
        )

        assertEquals(CachedWidgetError("HTTP 404"), outcome.errorToPersist)
    }

    @Test
    fun `a failed fetch that falls back to a cached reading does not persist an error`() {
        val cached = CachedWidgetReading(sensorName = "Backyard Sensor", pm25Aqi = 42, lastUpdatedEpochSeconds = 500)

        val outcome = FreeAirWidgetStateReducer.reduce(
            Result.failure(RuntimeException("network down")),
            cachedReading = cached,
            sensorId = "12345",
            appWidgetId = 42,
        )

        // We have a good reading to fall back to, so there's no reason to shadow it with an
        // error -- the in-app preview should keep showing that reading too.
        assertNull(outcome.errorToPersist)
    }

    @Test
    fun `a failed fetch with a cache falls back to the last known reading, including its map url`() {
        val cached = CachedWidgetReading(
            sensorName = "Backyard Sensor",
            pm25Aqi = 42,
            lastUpdatedEpochSeconds = 500,
            mapUrl = "https://map.purpleair.com/1/l/m/i/mAQI/a10/p2592000/cC0?select=12345#14.0/47.6062/-122.3321",
        )

        val outcome = FreeAirWidgetStateReducer.reduce(
            Result.failure(RuntimeException("network down")),
            cachedReading = cached,
            sensorId = "12345",
            appWidgetId = 42,
        )

        assertEquals(
            FreeAirWidgetState.Loaded(
                sensorName = "Backyard Sensor",
                pm25Aqi = 42,
                category = AqiCategory.forAqi(42),
                lastUpdated = Instant.ofEpochSecond(500),
                mapUrl = cached.mapUrl,
            ),
            outcome.state,
        )
        // Unchanged -- we didn't get fresh data, so there's nothing new to persist.
        assertEquals(cached, outcome.cacheToPersist)
    }
}
