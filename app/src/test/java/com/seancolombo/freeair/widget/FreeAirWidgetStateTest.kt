package com.seancolombo.freeair.widget

import com.seancolombo.freeair.airquality.AirQualityReading
import com.seancolombo.freeair.airquality.AqiCategory
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class FreeAirWidgetStateTest {
    @Test
    fun `a successful reading maps to Loaded with the computed AQI and category`() {
        val reading = AirQualityReading(
            sensorId = "12345",
            sensorName = "Backyard Sensor",
            pm25 = 20.0,
            temperatureFahrenheit = 72.0,
            humidityPercent = 45,
            lastUpdated = Instant.EPOCH,
        )

        val state = Result.success(reading).toWidgetState()

        assertEquals(
            FreeAirWidgetState.Loaded(
                sensorName = "Backyard Sensor",
                pm25Aqi = 71,
                category = AqiCategory.MODERATE,
            ),
            state,
        )
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

        val state = Result.success(reading).toWidgetState() as FreeAirWidgetState.Loaded

        assertEquals("Sensor 12345", state.sensorName)
    }

    @Test
    fun `a failed fetch maps to Error with the failure message`() {
        val state = Result.failure<AirQualityReading>(RuntimeException("network down")).toWidgetState()

        assertEquals(FreeAirWidgetState.Error("network down"), state)
    }
}
