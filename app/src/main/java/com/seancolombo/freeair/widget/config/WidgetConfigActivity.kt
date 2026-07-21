package com.seancolombo.freeair.widget.config

import android.appwidget.AppWidgetManager
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
import com.seancolombo.freeair.widget.EXTRA_EXPLICIT_REQUEST

/**
 * Launched three ways: by the system right after a user drags the widget onto their home screen
 * (via the `android:configure` / `APPWIDGET_CONFIGURE` wiring), by `WidgetPinner`'s
 * `successCallback` (some launchers don't honor `android:configure` after `requestPinAppWidget`,
 * so this is a fallback trigger for those -- see its own doc comment), and by MainActivity when
 * the user taps an existing widget's preview card to edit it. All three converge on
 * [WidgetConfigScreen], which uses [EXTRA_EXPLICIT_REQUEST] to tell the deliberate, in-app paths
 * (tap-to-edit, the widget's own "tap to set up") apart from the system-built ones, so a
 * redundant relaunch of an already-configured widget can be a silent no-op instead of showing
 * the form again with nothing to do -- see [WidgetConfigScreen] for why that redundancy happens.
 */
class WidgetConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Per the platform contract for APPWIDGET_CONFIGURE: assume cancelled unless we
        // explicitly set RESULT_OK after a successful save.
        setResult(RESULT_CANCELED)

        val appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        val isExplicitRequest = intent?.getBooleanExtra(EXTRA_EXPLICIT_REQUEST, false) ?: false

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            FreeAirTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WidgetConfigScreen(
                        appWidgetId = appWidgetId,
                        isExplicitRequest = isExplicitRequest,
                        modifier = Modifier.padding(innerPadding),
                        onSaved = {
                            setResult(
                                RESULT_OK,
                                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId),
                            )
                            finish()
                        },
                        onCancelled = { finish() },
                    )
                }
            }
        }
    }
}
