package com.seancolombo.freeair.airquality.purpleair

import com.seancolombo.freeair.airquality.AirQualitySensorConfig
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val SAMPLE_JSON = """
    {
      "sensor": {
        "sensor_index": 12345,
        "name": "Backyard Sensor",
        "last_seen": 1689999900,
        "pm2.5": 8.4,
        "humidity": 45,
        "temperature": 72,
        "latitude": 47.6062,
        "longitude": -122.3321,
        "location_type": 1
      }
    }
"""

private class FakePurpleAirHttpClient(
    private val response: (sensorId: String, apiKey: String) -> String,
) : PurpleAirHttpClient {
    var lastSensorId: String? = null
    var lastApiKey: String? = null

    override suspend fun getSensorJson(sensorId: String, apiKey: String): String {
        lastSensorId = sensorId
        lastApiKey = apiKey
        return response(sensorId, apiKey)
    }
}

class PurpleAirProviderTest {
    @Test
    fun `maps a successful response to a domain reading`() = runTest {
        val httpClient = FakePurpleAirHttpClient { _, _ -> SAMPLE_JSON }
        val provider = PurpleAirProvider(httpClient)
        val config = AirQualitySensorConfig(apiKey = "test-key", sensorId = "12345")

        val result = provider.fetchReading(config)

        assertTrue(result.isSuccess)
        val reading = result.getOrThrow()
        assertEquals("12345", reading.sensorId)
        assertEquals("Backyard Sensor", reading.sensorName)
        assertEquals(8.4, reading.pm25, 0.0)
        assertEquals(72.0, reading.temperatureFahrenheit)
        assertEquals(45, reading.humidityPercent)
        assertEquals(Instant.ofEpochSecond(1689999900L), reading.lastUpdated)
        assertEquals(47.6062, reading.latitude!!, 0.0)
        assertEquals(-122.3321, reading.longitude!!, 0.0)
        assertTrue(reading.isIndoor)
        assertEquals("12345", httpClient.lastSensorId)
        assertEquals("test-key", httpClient.lastApiKey)
    }

    @Test
    fun `an outdoor sensor's location_type maps to isIndoor false`() = runTest {
        val outdoorJson = """
            {
              "sensor": {
                "sensor_index": 12345,
                "name": "Backyard Sensor",
                "last_seen": 1689999900,
                "pm2.5": 8.4,
                "location_type": 0
              }
            }
        """
        val httpClient = FakePurpleAirHttpClient { _, _ -> outdoorJson }
        val provider = PurpleAirProvider(httpClient)
        val config = AirQualitySensorConfig(apiKey = "test-key", sensorId = "12345")

        val result = provider.fetchReading(config)

        assertFalse(result.getOrThrow().isIndoor)
    }

    @Test
    fun `wraps http failures as a failed Result instead of throwing`() = runTest {
        val httpClient = FakePurpleAirHttpClient { _, _ -> error("network down") }
        val provider = PurpleAirProvider(httpClient)
        val config = AirQualitySensorConfig(apiKey = "test-key", sensorId = "12345")

        val result = provider.fetchReading(config)

        assertTrue(result.isFailure)
    }
}
