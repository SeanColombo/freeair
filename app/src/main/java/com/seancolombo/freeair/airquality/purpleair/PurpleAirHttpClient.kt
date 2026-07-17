package com.seancolombo.freeair.airquality.purpleair

/** Thin network boundary so tests can supply canned JSON instead of hitting PurpleAir. */
interface PurpleAirHttpClient {
    suspend fun getSensorJson(sensorId: String, apiKey: String): String
}
