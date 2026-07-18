package com.seancolombo.freeair.widget

/**
 * The last fetch failure, persisted so the in-app preview (which never fetches on its own) can
 * show the same failure the widget itself hit, instead of being stuck on "Loading" forever. Only
 * ever written when there's no [CachedWidgetReading] yet to fall back to -- once a widget has a
 * successful reading, later failures fall back to that instead (see
 * [FreeAirWidgetStateReducer]), so this never gets written again after that point.
 */
data class CachedWidgetError(val message: String)
