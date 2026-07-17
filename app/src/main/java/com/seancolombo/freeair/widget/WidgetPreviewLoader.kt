package com.seancolombo.freeair.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition

/**
 * Read-only snapshot of each placed FreeAir widget's last known state, for showing "your
 * widgets" inside the app. Never triggers a network fetch -- a widget instance with no cache
 * yet (just placed, hasn't updated once) shows as [FreeAirWidgetState.Loading].
 */
suspend fun loadWidgetPreviews(context: Context): List<FreeAirWidgetState> {
    val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(FreeAirWidget::class.java)
    return glanceIds.map { id ->
        val prefs = getAppWidgetState<Preferences>(context, PreferencesGlanceStateDefinition, id)
        prefs.toCachedWidgetReading()?.toLoadedState() ?: FreeAirWidgetState.Loading
    }
}
