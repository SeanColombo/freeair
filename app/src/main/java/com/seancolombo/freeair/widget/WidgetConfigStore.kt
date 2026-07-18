package com.seancolombo.freeair.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition

suspend fun loadWidgetSensorConfig(context: Context, glanceId: GlanceId): WidgetSensorConfig =
    getAppWidgetState<Preferences>(context, PreferencesGlanceStateDefinition, glanceId).toWidgetSensorConfig()

/** Persists the new sensor for this widget instance, then re-renders it (and any others). */
suspend fun saveWidgetSensorConfig(context: Context, glanceId: GlanceId, config: WidgetSensorConfig) {
    updateAppWidgetState(context, glanceId) { prefs -> prefs.putWidgetSensorConfig(config) }
    FreeAirWidget().updateAll(context)
}
