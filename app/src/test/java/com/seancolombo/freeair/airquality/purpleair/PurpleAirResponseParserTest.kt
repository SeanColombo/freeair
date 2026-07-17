package com.seancolombo.freeair.airquality.purpleair

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PurpleAirResponseParserTest {
    @Test
    fun `parses a full sensor response`() {
        val json = """
            {
              "api_version": "V1.0.11-0.0.41",
              "time_stamp": 1690000000,
              "data_time_stamp": 1689999950,
              "sensor": {
                "sensor_index": 12345,
                "name": "Backyard Sensor",
                "last_seen": 1689999900,
                "pm2.5": 8.4,
                "humidity": 45,
                "temperature": 72
              }
            }
        """.trimIndent()

        val response = PurpleAirResponseParser.parse(json)

        assertEquals(12345, response.sensorIndex)
        assertEquals("Backyard Sensor", response.name)
        assertEquals(8.4, response.pm25, 0.0)
        assertEquals(72.0, response.temperatureFahrenheit)
        assertEquals(45, response.humidityPercent)
        assertEquals(1689999900L, response.lastSeenEpochSeconds)
    }

    @Test
    fun `treats missing optional fields as null`() {
        val json = """
            {
              "sensor": {
                "sensor_index": 12345,
                "last_seen": 1689999900,
                "pm2.5": 8.4
              }
            }
        """.trimIndent()

        val response = PurpleAirResponseParser.parse(json)

        assertEquals("", response.name)
        assertNull(response.temperatureFahrenheit)
        assertNull(response.humidityPercent)
    }
}
