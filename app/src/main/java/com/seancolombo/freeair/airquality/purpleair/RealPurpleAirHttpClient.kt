package com.seancolombo.freeair.airquality.purpleair

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val SENSORS_ENDPOINT = "https://api.purpleair.com/v1/sensors/"
private const val KEYS_ENDPOINT = "https://api.purpleair.com/v1/keys"
private const val REQUESTED_FIELDS = "name,pm2.5,humidity,temperature,last_seen,latitude,longitude,location_type"
private const val TIMEOUT_MILLIS = 10_000

class RealPurpleAirHttpClient(
    private val baseUrl: String = SENSORS_ENDPOINT,
) : PurpleAirHttpClient {
    override suspend fun getSensorJson(sensorId: String, apiKey: String): String =
        get("$baseUrl$sensorId?fields=$REQUESTED_FIELDS", apiKey)

    override suspend fun checkApiKey(apiKey: String): String = get(KEYS_ENDPOINT, apiKey)

    private suspend fun get(urlString: String, apiKey: String): String =
        withContext(Dispatchers.IO) {
            val connection = URL(urlString).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = TIMEOUT_MILLIS
                connection.readTimeout = TIMEOUT_MILLIS
                connection.setRequestProperty("X-API-Key", apiKey)

                val responseCode = connection.responseCode
                val body = (
                    if (responseCode in 200..299) connection.inputStream else connection.errorStream
                    ).bufferedReader().use { it.readText() }

                check(responseCode in 200..299) {
                    "PurpleAir API request failed with HTTP $responseCode: $body"
                }
                body
            } finally {
                connection.disconnect()
            }
        }
}
