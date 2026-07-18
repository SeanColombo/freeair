package com.seancolombo.freeair.widget.config

import android.appwidget.AppWidgetManager
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

/** Used by MainActivity's tap-to-edit; pulled out so the exact Intent shape is unit testable. */
fun buildWidgetConfigIntent(context: Context, appWidgetId: Int): Intent =
    Intent(context, WidgetConfigActivity::class.java)
        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

/**
 * Launched two ways: by the system right after a user drags the widget onto their home screen
 * (via the `android:configure` / `APPWIDGET_CONFIGURE` wiring), and by MainActivity when the
 * user taps an existing widget's preview card to edit it. Both converge on [WidgetConfigScreen].
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
