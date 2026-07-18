package com.seancolombo.freeair.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

/**
 * Wraps the platform's "pin to home screen" flow. There's no API for an app to drag a widget
 * onto the home screen itself -- `requestPinAppWidget` is the real mechanism: the app triggers
 * it with a tap, and the launcher takes over from there. Most modern launchers (including
 * Pixel's) render that as a floating widget preview the user drags into place themselves, so
 * the end result feels close to a manual drag even though our side of it is just a tap.
 */
object WidgetPinner {
    fun isSupported(context: Context): Boolean =
        AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported

    fun requestPin(context: Context) {
        val provider = ComponentName(context, FreeAirWidgetReceiver::class.java)
        AppWidgetManager.getInstance(context).requestPinAppWidget(provider, null, null)
    }
}
