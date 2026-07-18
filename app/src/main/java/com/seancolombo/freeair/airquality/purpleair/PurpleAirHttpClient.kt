package com.seancolombo.freeair.airquality.purpleair

/** Thin network boundary so tests can supply canned JSON instead of hitting PurpleAir. */
interface PurpleAirHttpClient {
    suspend fun getSensorJson(sensorId: String, apiKey: String): String

    /** Raw JSON from PurpleAir's "check API key" endpoint; throws if the key is invalid. */
    suspend fun checkApiKey(apiKey: String): String
}
