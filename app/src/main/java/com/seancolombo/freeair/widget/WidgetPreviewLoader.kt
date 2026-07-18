package com.seancolombo.freeair.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition

/** Carries the Android-framework widget id alongside its state, so a tap can open config for it. */
data class WidgetPreviewItem(val appWidgetId: Int, val state: FreeAirWidgetState)

/**
 * Read-only snapshot of each placed FreeAir widget's last known state, for showing "your
 * widgets" inside the app. Never triggers a network fetch -- an unconfigured widget shows as
 * [FreeAirWidgetState.NeedsSetup]; a configured one shows its last cached reading, or the last
 * cached failure (e.g. an invalid sensor ID) if it never had a successful reading, or
 * [FreeAirWidgetState.Loading] if neither exists yet (just set up, hasn't fetched once). The
 * reading/error cache is shared with the widget's own render path (see [FreeAirWidget]), so
 * whichever one fetches first updates what the other shows here too.
 */
suspend fun loadWidgetPreviews(context: Context): List<WidgetPreviewItem> {
    val manager = GlanceAppWidgetManager(context)
    val glanceIds = manager.getGlanceIds(FreeAirWidget::class.java)
    return glanceIds.map { id ->
        val appWidgetId = manager.getAppWidgetId(id)
        val prefs = getAppWidgetState<Preferences>(context, PreferencesGlanceStateDefinition, id)
        val sensorConfig = prefs.toWidgetSensorConfig()
        val state = if (sensorConfig == null) {
            FreeAirWidgetState.NeedsSetup(appWidgetId)
        } else {
            prefs.toCachedWidgetReading()?.toLoadedState()
                ?: prefs.toCachedWidgetError()?.let { FreeAirWidgetState.Error(it.message, sensorConfig.sensorId, appWidgetId) }
                ?: FreeAirWidgetState.Loading
        }
        WidgetPreviewItem(appWidgetId = appWidgetId, state = state)
    }
}
