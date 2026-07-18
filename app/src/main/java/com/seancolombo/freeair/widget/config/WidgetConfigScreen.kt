package com.seancolombo.freeair.widget.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.seancolombo.freeair.widget.WidgetErrorMessageFormatter
import com.seancolombo.freeair.widget.loadCachedWidgetError
import com.seancolombo.freeair.widget.loadWidgetSensorConfig
import com.seancolombo.freeair.widget.saveWidgetSensorConfig
import kotlinx.coroutines.launch

/**
 * The one config surface both entry points converge on: the system launches this (via
 * `WidgetConfigActivity`) right after a user drags the widget onto their home screen, and
 * tapping an existing widget's preview card in [com.seancolombo.freeair.MainActivity] opens
 * the same screen in "edit" mode for that instance.
 */
@Composable
fun WidgetConfigScreen(
    appWidgetId: Int,
    onSaved: () -> Unit,
    onCancelled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var model by remember { mutableStateOf<WidgetConfigModel?>(null) }
    var lastErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(appWidgetId) {
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
        val currentSensorId = loadWidgetSensorConfig(context, glanceId).sensorId
        lastErrorMessage = loadCachedWidgetError(context, glanceId)
            ?.let { WidgetErrorMessageFormatter.format(it.message, currentSensorId) }
        model = WidgetConfigModel(
            initialSensorId = currentSensorId,
            onSave = { config -> saveWidgetSensorConfig(context, glanceId, config) },
        )
    }

    val currentModel = model
    if (currentModel == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Configure widget", style = MaterialTheme.typography.headlineSmall)
        lastErrorMessage?.let { message ->
            Column {
                Text(
                    text = "Last error",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        OutlinedTextField(
            value = currentModel.sensorId,
            onValueChange = currentModel::onSensorIdChanged,
            label = { Text("Sensor ID") },
            supportingText = { Text("Find this in your sensor's PurpleAir map URL.") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onCancelled) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                enabled = currentModel.canSave,
                onClick = {
                    coroutineScope.launch {
                        if (currentModel.save()) onSaved()
                    }
                },
            ) {
                Text("Save")
            }
        }
    }
}
