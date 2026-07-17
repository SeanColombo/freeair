package com.seancolombo.freeair.airquality.purpleair

import com.seancolombo.freeair.BuildConfig
import com.seancolombo.freeair.airquality.AirQualitySensorConfig
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Hits the real PurpleAir API to catch drift our unit tests can't: an expired API key, a
 * deleted/renamed sensor, or a PurpleAir response shape change. Per AGENTS.md, this doesn't
 * need to run on every build -- just run it manually (`./gradlew test`) before committing
 * changes to the PurpleAir client.
 *
 * Skips automatically unless purpleair.apiKey / purpleair.sensorId are set in local.properties.
 */
class PurpleAirIntegrationTest {
    @Test
    fun `fetches a real reading from the PurpleAir API`() = runTest {
        assumeTrue(
            "Set purpleair.apiKey and purpleair.sensorId in local.properties to run this test",
            BuildConfig.PURPLEAIR_API_KEY.isNotBlank() && BuildConfig.PURPLEAIR_SENSOR_ID.isNotBlank(),
        )

        val provider = PurpleAirProvider()
        val config = AirQualitySensorConfig(
            apiKey = BuildConfig.PURPLEAIR_API_KEY,
            sensorId = BuildConfig.PURPLEAIR_SENSOR_ID,
        )

        val result = provider.fetchReading(config)

        assertTrue("Expected a successful reading but got: $result", result.isSuccess)
        val reading = result.getOrThrow()
        assertEquals(BuildConfig.PURPLEAIR_SENSOR_ID, reading.sensorId)
        assertTrue("pm2.5 should be non-negative, was ${reading.pm25}", reading.pm25 >= 0.0)
    }
}
