package com.seancolombo.freeair.widget.config

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.seancolombo.freeair.airquality.purpleair.PurpleAirApiKeyChecker
import com.seancolombo.freeair.widget.saveApiKey
import kotlinx.coroutines.launch

private const val PURPLEAIR_GET_API_KEY_URL = "https://develop.purpleair.com/dashboards/keys"

/**
 * Shown by [WidgetConfigScreen] in place of the sensor-ID form whenever no PurpleAir API key has
 * been saved yet (app-global -- see [com.seancolombo.freeair.widget.ApiKeyStore]). Kept as its
 * own screen/model rather than folded into the sensor form, since it only ever needs to run
 * once, while the sensor form can be shown repeatedly (once per widget).
 */
@Composable
fun ApiKeySetupScreen(onContinue: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val model = remember {
        ApiKeySetupModel(
            checkApiKey = { key -> PurpleAirApiKeyChecker().checkApiKey(key) },
            onVerified = { key -> saveApiKey(context, key) },
        )
    }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Add your PurpleAir API key", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "FreeAir needs a free API key from PurpleAir to fetch your sensor's data. " +
                "You only need to do this once -- it's shared by all your widgets.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, PURPLEAIR_GET_API_KEY_URL.toUri()))
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Get a free API key from PurpleAir")
        }
        OutlinedTextField(
            value = model.apiKey,
            onValueChange = model::onApiKeyChanged,
            label = { Text("API Key") },
            supportingText = { Text("Paste the key PurpleAir gave you here.") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        val status = model.status
        if (status is ApiKeySetupStatus.Failure) {
            Text(
                text = status.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Button(
            enabled = model.canVerify,
            onClick = { coroutineScope.launch { model.verify() } },
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (status is ApiKeySetupStatus.Verifying) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Verify")
            }
        }
    }

    if (model.status is ApiKeySetupStatus.Success) {
        AlertDialog(
            onDismissRequest = {}, // must tap Continue -- there's nothing to dismiss back to
            title = { Text("You're all set!") },
            text = { Text("✅ Your PurpleAir API key works.") },
            confirmButton = {
                Button(onClick = onContinue) {
                    Text("Continue")
                }
            },
        )
    }
}
