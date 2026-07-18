package com.seancolombo.freeair.widget

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

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
private val KEY_READING_SENSOR_ID = stringPreferencesKey("reading_sensor_id")
private val KEY_ERROR_MESSAGE = stringPreferencesKey("error_message")
private val KEY_ERROR_SENSOR_ID = stringPreferencesKey("error_sensor_id")
private val KEY_IS_INDOOR = booleanPreferencesKey("is_indoor")

internal fun Preferences.toCachedWidgetReading(): CachedWidgetReading? {
    val sensorName = this[KEY_SENSOR_NAME] ?: return null
    val pm25Aqi = this[KEY_PM25_AQI] ?: return null
    val lastUpdatedEpochSeconds = this[KEY_LAST_UPDATED_EPOCH_SECONDS] ?: return null
    // Also missing for reading data written before sensorId existed on this type -- drop it
    // rather than guess, same reasoning as CachedWidgetError.
    val sensorId = this[KEY_READING_SENSOR_ID] ?: return null
    return CachedWidgetReading(
        sensorId,
        sensorName,
        pm25Aqi,
        lastUpdatedEpochSeconds,
        mapUrl = this[KEY_MAP_URL],
        isIndoor = this[KEY_IS_INDOOR] ?: false,
    )
}

internal fun MutablePreferences.putCachedWidgetReading(cached: CachedWidgetReading) {
    this[KEY_READING_SENSOR_ID] = cached.sensorId
    this[KEY_SENSOR_NAME] = cached.sensorName
    this[KEY_PM25_AQI] = cached.pm25Aqi
    this[KEY_LAST_UPDATED_EPOCH_SECONDS] = cached.lastUpdatedEpochSeconds
    this[KEY_IS_INDOOR] = cached.isIndoor
    if (cached.mapUrl != null) {
        this[KEY_MAP_URL] = cached.mapUrl
    } else {
        this.remove(KEY_MAP_URL)
    }
}

internal fun Preferences.toCachedWidgetError(): CachedWidgetError? {
    val message = this[KEY_ERROR_MESSAGE] ?: return null
    // Also missing for error data written before sensorId existed on this type -- drop it rather
    // than guess, since a mismatched sensorId is exactly the confusing state this is meant to
    // avoid. It'll be replaced by a fresh, correctly-tagged error the next time a fetch fails.
    val sensorId = this[KEY_ERROR_SENSOR_ID] ?: return null
    return CachedWidgetError(message, sensorId)
}

internal fun MutablePreferences.putCachedWidgetError(error: CachedWidgetError) {
    this[KEY_ERROR_MESSAGE] = error.message
    this[KEY_ERROR_SENSOR_ID] = error.sensorId
}

// Null means "never configured" -- deliberately no BuildConfig fallback here. A widget with no
// saved sensor should render as NeedsSetup, not silently reuse some other default and look like
// a copy of an already-configured widget.
internal fun Preferences.toWidgetSensorConfig(): WidgetSensorConfig? =
    this[KEY_SENSOR_ID]?.let { WidgetSensorConfig(sensorId = it) }

internal fun MutablePreferences.putWidgetSensorConfig(config: WidgetSensorConfig) {
    this[KEY_SENSOR_ID] = config.sensorId
}
