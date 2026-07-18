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
 * widgets" inside the app. Never triggers a network fetch -- a widget instance with no cache
 * yet (just placed, hasn't updated once) shows as [FreeAirWidgetState.Loading].
 */
suspend fun loadWidgetPreviews(context: Context): List<WidgetPreviewItem> {
    val manager = GlanceAppWidgetManager(context)
    val glanceIds = manager.getGlanceIds(FreeAirWidget::class.java)
    return glanceIds.map { id ->
        val prefs = getAppWidgetState<Preferences>(context, PreferencesGlanceStateDefinition, id)
        val state = prefs.toCachedWidgetReading()?.toLoadedState() ?: FreeAirWidgetState.Loading
        WidgetPreviewItem(appWidgetId = manager.getAppWidgetId(id), state = state)
    }
}
