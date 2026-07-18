package com.seancolombo.freeair.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import com.seancolombo.freeair.widget.config.WidgetConfigActivity

/**
 * Used by MainActivity's tap-to-edit and by the widget's own "tap to set up" state -- pulled
 * out so the exact Intent shape is unit testable, and so both call sites (in different
 * packages) share one implementation instead of building this by hand twice.
 */
fun buildWidgetConfigIntent(context: Context, appWidgetId: Int): Intent =
    Intent(context, WidgetConfigActivity::class.java)
        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
