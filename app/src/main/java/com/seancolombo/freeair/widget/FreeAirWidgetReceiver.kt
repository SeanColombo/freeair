package com.seancolombo.freeair.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class FreeAirWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FreeAirWidget()

    // Fires once, when the first widget instance is placed -- so nothing polls in the
    // background unless a widget is actually on a home screen.
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        FreeAirWidgetScheduler.schedule(context)
    }

    // Fires once, when the last widget instance is removed.
    override fun onDisabled(context: Context) {
        FreeAirWidgetScheduler.cancel(context)
        super.onDisabled(context)
    }
}
