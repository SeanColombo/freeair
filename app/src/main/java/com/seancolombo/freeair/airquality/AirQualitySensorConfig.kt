package com.seancolombo.freeair.airquality

/** Credentials needed to fetch one sensor's reading from an [AirQualityProvider]. */
data class AirQualitySensorConfig(
    val apiKey: String,
    val sensorId: String,
)
