package com.seancolombo.freeair.widget

/**
 * Turns a raw error message (whatever the underlying HTTP client captured, cached verbatim in
 * [CachedWidgetError] and [FreeAirWidgetState.Error]) into something a user can actually act on.
 * Deliberately lives at render time rather than baked into the cached/stored value: an app update
 * that recognizes a new pattern here immediately improves how an already-cached error displays,
 * with no re-fetch needed, and the raw message stays around for debugging either way.
 *
 * Unrecognized errors fall back to the raw message as-is, so nothing is ever hidden -- just not
 * yet prettified.
 */
object WidgetErrorMessageFormatter {
    // PurpleAir's documented API error code for "no sensor with that ID", e.g. a 404 body of
    // {"error": "NotFoundError", "description": "Cannot find a sensor with the provided
    // parameters."}. Matched as a substring rather than parsed as JSON, since the raw message
    // also has an "HTTP 404: " prefix we don't otherwise care about.
    private const val NOT_FOUND_ERROR_CODE = "\"NotFoundError\""

    fun format(rawMessage: String, sensorId: String): String =
        if (rawMessage.contains(NOT_FOUND_ERROR_CODE)) {
            "Error: Sensor ID $sensorId not found."
        } else {
            rawMessage
        }
}
