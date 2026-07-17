package com.seancolombo.freeair.widget

import androidx.datastore.preferences.core.emptyPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WidgetPreferencesStoreTest {
    @Test
    fun `a cached reading round-trips through preferences`() {
        val cached = CachedWidgetReading(
            sensorName = "Backyard Sensor",
            pm25Aqi = 71,
            lastUpdatedEpochSeconds = 1_000,
            mapUrl = "https://map.purpleair.com/1/l/m/i/mAQI/a10/p2592000/cC0?select=12345#14.0/47.6/-122.3",
        )

        val prefs = emptyPreferences().toMutablePreferences().apply { putCachedWidgetReading(cached) }

        assertEquals(cached, prefs.toCachedWidgetReading())
    }

    @Test
    fun `a null map url round-trips as null, not a missing key`() {
        val cached = CachedWidgetReading(sensorName = "Backyard Sensor", pm25Aqi = 71, lastUpdatedEpochSeconds = 1_000)

        val prefs = emptyPreferences().toMutablePreferences().apply { putCachedWidgetReading(cached) }

        assertNull(prefs.toCachedWidgetReading()?.mapUrl)
    }

    @Test
    fun `incomplete preferences produce no cached reading`() {
        assertNull(emptyPreferences().toCachedWidgetReading())
    }

    @Test
    fun `sensor config falls back to the BuildConfig default when unset`() {
        val config = emptyPreferences().toWidgetSensorConfig()

        assertEquals(com.seancolombo.freeair.BuildConfig.PURPLEAIR_SENSOR_ID, config.sensorId)
    }
}
