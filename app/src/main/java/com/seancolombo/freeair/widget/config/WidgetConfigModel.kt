package com.seancolombo.freeair.widget.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.seancolombo.freeair.widget.WidgetSensorConfig

/**
 * Plain state holder for the config screen -- not an Android ViewModel, just observable state
 * plus validation/save logic, so it's testable with a fake [onSave] and no Activity/Context.
 * The single sensor-ID field today is deliberately the whole form: it's positioned to later
 * grow a "paste widget code" field that feeds the same [sensorId] via
 * `PurpleAirWidgetCodeParser`, without changing this class's shape.
 */
class WidgetConfigModel(
    initialSensorId: String,
    private val onSave: suspend (WidgetSensorConfig) -> Unit,
) {
    var sensorId by mutableStateOf(initialSensorId)
        private set

    val canSave: Boolean
        get() = sensorId.isNotBlank()

    fun onSensorIdChanged(value: String) {
        sensorId = value
    }

    /** Returns false without calling [onSave] if the current input isn't valid. */
    suspend fun save(): Boolean {
        if (!canSave) return false
        onSave(WidgetSensorConfig(sensorId.trim()))
        return true
    }
}
