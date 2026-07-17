package com.seancolombo.freeair.airquality.purpleair

import org.json.JSONObject

/** Pure JSON parsing, kept separate from [PurpleAirHttpClient] so it's testable with static fixtures. */
object PurpleAirResponseParser {
    fun parse(json: String): PurpleAirSensorResponse {
        val sensor = JSONObject(json).getJSONObject("sensor")
        return PurpleAirSensorResponse(
            sensorIndex = sensor.getInt("sensor_index"),
            name = sensor.optString("name", ""),
            pm25 = sensor.getDouble("pm2.5"),
            temperatureFahrenheit = if (sensor.has("temperature")) sensor.getDouble("temperature") else null,
            humidityPercent = if (sensor.has("humidity")) sensor.getInt("humidity") else null,
            lastSeenEpochSeconds = sensor.getLong("last_seen"),
            latitude = if (sensor.has("latitude")) sensor.getDouble("latitude") else null,
            longitude = if (sensor.has("longitude")) sensor.getDouble("longitude") else null,
        )
    }
}
