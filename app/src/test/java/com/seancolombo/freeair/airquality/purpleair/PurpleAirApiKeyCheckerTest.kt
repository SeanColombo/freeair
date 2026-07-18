package com.seancolombo.freeair.airquality.purpleair

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeApiKeyHttpClient(
    private val response: (apiKey: String) -> String,
) : PurpleAirHttpClient {
    var lastApiKey: String? = null

    override suspend fun getSensorJson(sensorId: String, apiKey: String): String =
        error("not used by this test")

    override suspend fun checkApiKey(apiKey: String): String {
        lastApiKey = apiKey
        return response(apiKey)
    }
}

class PurpleAirApiKeyCheckerTest {
    // Response shapes confirmed against the real endpoint (https://api.purpleair.com/v1/keys).
    private val validKeyJson = """
        {
          "api_version" : "V1.2.2-1.1.45",
          "time_stamp" : 1784409161,
          "api_key_type" : "READ"
        }
    """

    @Test
    fun `a valid key succeeds`() = runTest {
        val httpClient = FakeApiKeyHttpClient { validKeyJson }
        val checker = PurpleAirApiKeyChecker(httpClient)

        val result = checker.checkApiKey("a-real-key")

        assertTrue(result.isSuccess)
        assertTrue(httpClient.lastApiKey == "a-real-key")
    }

    @Test
    fun `an invalid key fails`() = runTest {
        val httpClient = FakeApiKeyHttpClient {
            error(
                "PurpleAir API request failed with HTTP 403: " +
                    "{\"error\":\"ApiKeyInvalidError\",\"description\":\"The provided api_key was not valid.\"}",
            )
        }
        val checker = PurpleAirApiKeyChecker(httpClient)

        val result = checker.checkApiKey("not-a-real-key")

        assertTrue(result.isFailure)
    }
}
