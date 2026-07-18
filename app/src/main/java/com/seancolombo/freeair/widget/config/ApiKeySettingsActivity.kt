package com.seancolombo.freeair.widget.config

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.seancolombo.freeair.ui.theme.FreeAirTheme

/**
 * Standalone entry point to [ApiKeySetupScreen] from the main app's menu -- unlike
 * [WidgetConfigActivity], this never needs an appWidgetId, so it works whether or not any widget
 * exists yet, and whether or not a key has already been saved (re-entering it just overwrites
 * the old one).
 */
class ApiKeySettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreeAirTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ApiKeySetupScreen(
                        onContinue = { finish() },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

fun buildApiKeySettingsIntent(context: Context): Intent = Intent(context, ApiKeySettingsActivity::class.java)
