package com.seancolombo.freeair.widget.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.seancolombo.freeair.airquality.purpleair.PurpleAirWidgetCodeParser
import com.seancolombo.freeair.widget.WidgetSensorConfig

/**
 * Plain state holder for the config screen -- not an Android ViewModel, just observable state
 * plus validation/save logic, so it's testable with a fake [onSave] and no Activity/Context.
 */
class WidgetConfigModel(
    initialSensorId: String,
    private val onSave: suspend (WidgetSensorConfig) -> Unit,
) {
    var sensorId by mutableStateOf(initialSensorId)
        private set

    val canSave: Boolean
        get() = sensorId.isNotBlank()

    /**
     * Users often paste something other than the bare sensor ID -- PurpleAir's "Widget Code"
     * HTML, that snippet's script URL alone, or the ID with stray underscores still around it.
     * Silently clean those down to just the ID via [PurpleAirWidgetCodeParser] rather than
     * making the user edit it by hand; anything else (including a plain ID) passes through as-is.
     *
     * Only attempted when the field grew by more than one character in a single change (a paste
     * landing all at once), not while typing one keystroke at a time -- otherwise, typing
     * something that happens to complete an "_<digits>_" match partway through (unlikely for a
     * real ID, but not impossible while composing other text) would get truncated mid-edit.
     */
    fun onSensorIdChanged(value: String) {
        val trimmed = value.trim()
        val isBulkInsert = trimmed.length - sensorId.length > 1
        sensorId = when {
            trimmed.isEmpty() || trimmed.all(Char::isDigit) -> trimmed
            isBulkInsert -> PurpleAirWidgetCodeParser.parseSensorIndex(trimmed)?.toString() ?: trimmed
            else -> trimmed
        }
    }

    /** Returns false without calling [onSave] if the current input isn't valid. */
    suspend fun save(): Boolean {
        if (!canSave) return false
        onSave(WidgetSensorConfig(sensorId.trim()))
        return true
    }
}
