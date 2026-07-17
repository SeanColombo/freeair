package com.seancolombo.freeair.airquality

import java.time.Instant

/**
 * A single sensor's reading, normalized across whatever monitor network supplied it
 * (PurpleAir today, potentially others later). Widget/UI code should only ever see this
 * type, never a supplier-specific one.
 */
data class AirQualityReading(
    val sensorId: String,
    val sensorName: String,
    val pm25: Double,
    val temperatureFahrenheit: Double?,
    val humidityPercent: Int?,
    val lastUpdated: Instant,
    val latitude: Double?,
    val longitude: Double?,
)
