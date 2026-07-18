package com.seancolombo.freeair.widget

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.seancolombo.freeair.BuildConfig

/**
 * Reads and writes the per-widget-instance state Glance already persists for us (via
 * `PreferencesGlanceStateDefinition`) -- both the last successful reading (for cache fallback
 * and in-app preview) and, going forward, per-widget configuration like the sensor to show.
 */
private val KEY_SENSOR_NAME = stringPreferencesKey("sensor_name")
private val KEY_PM25_AQI = intPreferencesKey("pm25_aqi")
private val KEY_LAST_UPDATED_EPOCH_SECONDS = longPreferencesKey("last_updated_epoch_seconds")
private val KEY_MAP_URL = stringPreferencesKey("map_url")
private val KEY_SENSOR_ID = stringPreferencesKey("sensor_id")

internal fun Preferences.toCachedWidgetReading(): CachedWidgetReading? {
    val sensorName = this[KEY_SENSOR_NAME] ?: return null
    val pm25Aqi = this[KEY_PM25_AQI] ?: return null
    val lastUpdatedEpochSeconds = this[KEY_LAST_UPDATED_EPOCH_SECONDS] ?: return null
    return CachedWidgetReading(sensorName, pm25Aqi, lastUpdatedEpochSeconds, mapUrl = this[KEY_MAP_URL])
}

internal fun MutablePreferences.putCachedWidgetReading(cached: CachedWidgetReading) {
    this[KEY_SENSOR_NAME] = cached.sensorName
    this[KEY_PM25_AQI] = cached.pm25Aqi
    this[KEY_LAST_UPDATED_EPOCH_SECONDS] = cached.lastUpdatedEpochSeconds
    if (cached.mapUrl != null) {
        this[KEY_MAP_URL] = cached.mapUrl
    } else {
        this.remove(KEY_MAP_URL)
    }
}

// Falls back to the app-level BuildConfig default until a widget has been configured.
internal fun Preferences.toWidgetSensorConfig(): WidgetSensorConfig =
    WidgetSensorConfig(sensorId = this[KEY_SENSOR_ID] ?: BuildConfig.PURPLEAIR_SENSOR_ID)

internal fun MutablePreferences.putWidgetSensorConfig(config: WidgetSensorConfig) {
    this[KEY_SENSOR_ID] = config.sensorId
}
