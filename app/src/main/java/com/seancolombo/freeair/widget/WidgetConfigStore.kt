package com.seancolombo.freeair.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.seancolombo.freeair.BuildConfig

/**
 * For the config screen's prefill only -- falls back to the app-level BuildConfig default so
 * the field isn't blank on a fresh widget. The widget's own rendering path does NOT use this
 * fallback; see [toWidgetSensorConfig].
 */
suspend fun loadWidgetSensorConfig(context: Context, glanceId: GlanceId): WidgetSensorConfig {
    val stored = getAppWidgetState<Preferences>(context, PreferencesGlanceStateDefinition, glanceId)
        .toWidgetSensorConfig()
    return stored ?: WidgetSensorConfig(sensorId = BuildConfig.PURPLEAIR_SENSOR_ID)
}

/** Persists the new sensor for this widget instance, then re-renders it (and any others). */
suspend fun saveWidgetSensorConfig(context: Context, glanceId: GlanceId, config: WidgetSensorConfig) {
    updateAppWidgetState(context, glanceId) { prefs -> prefs.putWidgetSensorConfig(config) }
    FreeAirWidget().updateAll(context)
}

/**
 * The last fetch failure for this widget, so the config screen can show it as context for why
 * the user ended up here -- same precedence as the widget/in-app preview's own rendering (see
 * [FreeAirWidgetStateReducer]): a cached error only means anything while there's no successful
 * reading yet, since a later success would otherwise leave a stale error visible here forever.
 * Also only trusted if it's for the sensor ID currently saved -- see [CachedWidgetError].
 */
suspend fun loadCachedWidgetError(context: Context, glanceId: GlanceId): CachedWidgetError? {
    val prefs = getAppWidgetState<Preferences>(context, PreferencesGlanceStateDefinition, glanceId)
    val sensorId = prefs.toWidgetSensorConfig()?.sensorId ?: return null
    val hasCurrentReading = prefs.toCachedWidgetReading()?.sensorId == sensorId
    if (hasCurrentReading) return null
    return prefs.toCachedWidgetError()?.takeIf { it.sensorId == sensorId }
}
