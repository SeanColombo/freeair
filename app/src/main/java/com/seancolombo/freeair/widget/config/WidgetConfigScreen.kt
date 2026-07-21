package com.seancolombo.freeair.widget.config

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.seancolombo.freeair.R
import com.seancolombo.freeair.widget.WidgetErrorMessageFormatter
import com.seancolombo.freeair.widget.loadApiKey
import com.seancolombo.freeair.widget.loadCachedWidgetError
import com.seancolombo.freeair.widget.loadWidgetSensorConfig
import com.seancolombo.freeair.widget.saveWidgetSensorConfig
import kotlinx.coroutines.launch

private const val PURPLEAIR_MAP_URL = "https://map.purpleair.com/"

/**
 * True when a widget that's already configured is being (re)launched by something other than a
 * deliberate in-app request -- see [WidgetConfigScreen]'s doc comment for why that happens (some
 * launchers re-fire `WidgetPinner`'s `successCallback` more than once). Pulled out of
 * [SensorIdConfigScreen]'s `LaunchedEffect` as a plain function so this decision rule is
 * unit-testable without Robolectric or Compose test infra -- neither reliably drives a
 * Composable's `LaunchedEffect` in this project's current test setup.
 */
internal fun shouldSkipRedundantRelaunch(isExplicitRequest: Boolean, currentSensorId: String): Boolean =
    !isExplicitRequest && currentSensorId.isNotBlank()

/**
 * The one config surface both entry points converge on: the system launches this (via
 * `WidgetConfigActivity`) right after a user drags the widget onto their home screen, and
 * tapping an existing widget's preview card in [com.seancolombo.freeair.MainActivity] opens
 * the same screen in "edit" mode for that instance.
 *
 * Shows [ApiKeySetupScreen] first if no PurpleAir API key has been saved yet (app-global, shared
 * by every widget), then [SensorIdConfigScreen] for the per-widget sensor ID. [isExplicitRequest]
 * (false only for the system-built launches -- see [WidgetConfigActivity]) lets
 * [SensorIdConfigScreen] silently finish instead of showing the form again for a widget that's
 * already configured, since some launchers re-fire `WidgetPinner`'s `successCallback` more than
 * once, well after the user already finished setup through it the first time.
 */
@Composable
fun WidgetConfigScreen(
    appWidgetId: Int,
    isExplicitRequest: Boolean,
    onSaved: () -> Unit,
    onCancelled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasApiKey by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        hasApiKey = loadApiKey(context) != null
    }

    when (hasApiKey) {
        null -> Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        false -> ApiKeySetupScreen(onContinue = { hasApiKey = true }, modifier = modifier)
        true -> SensorIdConfigScreen(
            appWidgetId = appWidgetId,
            isExplicitRequest = isExplicitRequest,
            onSaved = onSaved,
            onCancelled = onCancelled,
            modifier = modifier,
        )
    }
}

@Composable
private fun SensorIdConfigScreen(
    appWidgetId: Int,
    isExplicitRequest: Boolean,
    onSaved: () -> Unit,
    onCancelled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var model by remember { mutableStateOf<WidgetConfigModel?>(null) }
    var alreadyConfigured by remember { mutableStateOf(false) }
    var lastErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(appWidgetId) {
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
        val currentSensorId = loadWidgetSensorConfig(context, glanceId).sensorId

        if (shouldSkipRedundantRelaunch(isExplicitRequest, currentSensorId)) {
            // A system-triggered relaunch (not a deliberate tap-to-edit) for a widget that's
            // already configured -- nothing to do, so finish quietly rather than show the form
            // again with no obvious reason why.
            alreadyConfigured = true
            return@LaunchedEffect
        }
        lastErrorMessage = loadCachedWidgetError(context, glanceId)
            ?.let { WidgetErrorMessageFormatter.format(it.message, currentSensorId) }
        model = WidgetConfigModel(
            initialSensorId = currentSensorId,
            onSave = { config -> saveWidgetSensorConfig(context, glanceId, config) },
        )
    }

    LaunchedEffect(alreadyConfigured) {
        if (alreadyConfigured) onSaved()
    }

    // Also covers the brief window while alreadyConfigured is being determined.
    val currentModel = model
    if (currentModel == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val coroutineScope = rememberCoroutineScope()
    var showHelpImage by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
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
            supportingText = {
                SensorIdSupportingText(
                    showHelpImage = showHelpImage,
                    onToggleHelpImage = { showHelpImage = !showHelpImage },
                )
            },
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
        // Shown below Cancel/Save (rather than replacing the form) so the map-URL link in the
        // supporting text above stays reachable at the same time as the screenshot.
        if (showHelpImage) {
            Image(
                painter = painterResource(R.drawable.sensor_id_help),
                contentDescription = "Screenshot of the PurpleAir sensor page: tap \"Get This " +
                    "Widget\", then look for the number embedded in the code that appears " +
                    "(e.g. \"PurpleAirWidget_123456_...\") -- that number is the sensor ID.",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SensorIdSupportingText(showHelpImage: Boolean, onToggleHelpImage: () -> Unit) {
    val context = LocalContext.current
    val linkStyles = TextLinkStyles(
        style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline),
    )
    val openMapUrlListener = LinkInteractionListener {
        context.startActivity(Intent(Intent.ACTION_VIEW, PURPLEAIR_MAP_URL.toUri()))
    }
    val toggleHelpImageListener = LinkInteractionListener { onToggleHelpImage() }
    val text = buildAnnotatedString {
        append("Find this in your sensor's ")
        withLink(
            LinkAnnotation.Clickable(tag = "map_url", styles = linkStyles, linkInteractionListener = openMapUrlListener),
        ) {
            append("PurpleAir map URL")
        }
        append(". ")
        withLink(
            LinkAnnotation.Clickable(tag = "help", styles = linkStyles, linkInteractionListener = toggleHelpImageListener),
        ) {
            append(if (showHelpImage) "Hide help image." else "Need help finding it?")
        }
    }
    Text(text = text, style = MaterialTheme.typography.bodySmall)
}
