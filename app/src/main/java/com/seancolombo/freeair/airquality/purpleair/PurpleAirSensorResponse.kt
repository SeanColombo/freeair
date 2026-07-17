package com.seancolombo.freeair.airquality.purpleair

/** Raw shape of the "sensor" object in a PurpleAir `/v1/sensors/{id}` response. */
data class PurpleAirSensorResponse(
    val sensorIndex: Int,
    val name: String,
    val pm25: Double,
    val temperatureFahrenheit: Double?,
    val humidityPercent: Int?,
    val lastSeenEpochSeconds: Long,
)
