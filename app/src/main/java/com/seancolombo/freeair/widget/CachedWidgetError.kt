package com.seancolombo.freeair.widget

/**
 * The last fetch failure, persisted so the in-app preview (which never fetches on its own) can
 * show the same failure the widget itself hit, instead of being stuck on "Loading" forever. Only
 * ever written when there's no [CachedWidgetReading] yet to fall back to -- once a widget has a
 * successful reading, later failures fall back to that instead (see
 * [FreeAirWidgetStateReducer]), so this never gets written again after that point.
 *
 * sensorId is whichever sensor was configured when this failure happened. If the widget's
 * current sensor config no longer matches it (the user has since saved a different ID), this
 * error is stale and shouldn't be shown as if it were about the new one.
 */
data class CachedWidgetError(val message: String, val sensorId: String)
