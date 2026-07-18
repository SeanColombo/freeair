package com.seancolombo.freeair.airquality.purpleair

/**
 * Validates a PurpleAir API key against PurpleAir's own "check API key" endpoint, without
 * needing a sensor ID -- used by the one-time API key setup screen, before any sensor has been
 * chosen. Kept separate from [AirQualityProvider][com.seancolombo.freeair.airquality.AirQualityProvider]
 * since validating a raw key is a PurpleAir-specific concept with no obvious cross-supplier
 * equivalent, unlike fetching a reading.
 */
class PurpleAirApiKeyChecker(
    private val httpClient: PurpleAirHttpClient = RealPurpleAirHttpClient(),
) {
    suspend fun checkApiKey(apiKey: String): Result<Unit> = runCatching {
        httpClient.checkApiKey(apiKey)
        Unit
    }
}
