package com.seancolombo.freeair.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.seancolombo.freeair.widget.config.WidgetConfigActivity

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

        // Unlike dragging the widget out of the launcher's own widget tray, this pin flow does
        // NOT reliably auto-launch the configure activity afterward -- that's a real platform
        // gap, not something we can fix by changing our manifest wiring. The successCallback is
        // the documented way to hook "pinning finished": the system fills in EXTRA_APPWIDGET_ID
        // on this intent with the newly assigned id and fires it, so where the launcher honors
        // this callback, the user lands straight on config instead of needing the widget's own
        // "Tap to set up" fallback state. The PendingIntent must be mutable, or the system has
        // nowhere to put that id.
        val successCallback = PendingIntent.getActivity(
            context,
            0,
            Intent(context, WidgetConfigActivity::class.java),
            PendingIntent.FLAG_MUTABLE,
        )

        AppWidgetManager.getInstance(context).requestPinAppWidget(provider, null, successCallback)
    }
}
