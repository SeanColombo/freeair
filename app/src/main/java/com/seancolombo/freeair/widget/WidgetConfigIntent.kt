package com.seancolombo.freeair.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import com.seancolombo.freeair.widget.config.WidgetConfigActivity

/**
 * Marks an Intent as a deliberate, in-app request to open config -- as opposed to one the system
 * builds itself (`android:configure`'s own auto-launch, or `WidgetPinner`'s `successCallback`).
 * Some launchers re-fire that success callback's PendingIntent repeatedly, well after the
 * widget's already been configured through this same explicit path -- see [WidgetConfigActivity]
 * for how this flag is used to make that redundant relaunch a silent no-op instead of showing
 * the form again with nothing to do.
 */
const val EXTRA_EXPLICIT_REQUEST = "com.seancolombo.freeair.EXTRA_EXPLICIT_REQUEST"

/**
 * Used by MainActivity's tap-to-edit and by the widget's own "tap to set up" state -- pulled
 * out so the exact Intent shape is unit testable, and so both call sites (in different
 * packages) share one implementation instead of building this by hand twice.
 */
fun buildWidgetConfigIntent(context: Context, appWidgetId: Int): Intent =
    Intent(context, WidgetConfigActivity::class.java)
        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        .putExtra(EXTRA_EXPLICIT_REQUEST, true)
