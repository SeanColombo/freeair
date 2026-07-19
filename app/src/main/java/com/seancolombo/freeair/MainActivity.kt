package com.seancolombo.freeair

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.seancolombo.freeair.ui.AppTopBar
import com.seancolombo.freeair.ui.theme.FreeAirTheme
import com.seancolombo.freeair.widget.AddWidgetCard
import com.seancolombo.freeair.widget.FreeAirWidgetState
import com.seancolombo.freeair.widget.WidgetPreview
import com.seancolombo.freeair.widget.WidgetPreviewItem
import com.seancolombo.freeair.widget.buildWidgetConfigIntent
import com.seancolombo.freeair.widget.config.buildApiKeySettingsIntent
import com.seancolombo.freeair.widget.loadWidgetPreviews
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// How long we're willing to keep re-checking a freshly configured widget before giving up and
// leaving whatever state it's in on screen -- comfortably longer than the network timeout the
// widget's own fetch is bounded by, so a real result almost always arrives before this runs out.
private const val RELOAD_POLL_INTERVAL_MILLIS = 2_000L
private const val RELOAD_POLL_MAX_ATTEMPTS = 10

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreeAirTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { AppTopBar() },
                ) { innerPadding ->
                    WidgetManagerScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun WidgetManagerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var widgetStates by remember { mutableStateOf<List<WidgetPreviewItem>>(emptyList()) }

    // A freshly saved/added widget hasn't fetched yet, so it briefly shows Loading -- keep
    // re-checking for a bit rather than taking a single snapshot, so the list picks up the result
    // as soon as it's cached instead of only on the next resume (e.g. leaving and coming back).
    suspend fun reloadUntilResolved() {
        repeat(RELOAD_POLL_MAX_ATTEMPTS) { attempt ->
            widgetStates = loadWidgetPreviews(context)
            if (widgetStates.none { it.state is FreeAirWidgetState.Loading }) return
            if (attempt < RELOAD_POLL_MAX_ATTEMPTS - 1) delay(RELOAD_POLL_INTERVAL_MILLIS)
        }
    }

    LaunchedEffect(Unit) { reloadUntilResolved() }

    // Widgets can be added/removed from the home screen while this Activity isn't visible --
    // refresh whenever the user comes back to it so the list doesn't go stale.
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch { reloadUntilResolved() }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (widgetStates.isEmpty()) {
        EmptyState(modifier = modifier.fillMaxSize())
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(widgetStates, key = { it.appWidgetId }) { item ->
                WidgetPreview(
                    state = item.state,
                    onClick = { openWidgetConfig(context, item.appWidgetId) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                AddWidgetCard(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun openWidgetConfig(context: Context, appWidgetId: Int) {
    context.startActivity(buildWidgetConfigIntent(context, appWidgetId))
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "No FreeAir widgets yet",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Add the FreeAir widget to your home screen to see live air quality here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        AddWidgetCard(modifier = Modifier.fillMaxWidth())
    }
}
