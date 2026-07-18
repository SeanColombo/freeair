package com.seancolombo.freeair

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
import com.seancolombo.freeair.ui.theme.FreeAirTheme
import com.seancolombo.freeair.widget.AddWidgetCard
import com.seancolombo.freeair.widget.FreeAirWidgetState
import com.seancolombo.freeair.widget.WidgetPreview
import com.seancolombo.freeair.widget.loadWidgetPreviews
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreeAirTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WidgetManagerScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun WidgetManagerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var widgetStates by remember { mutableStateOf<List<FreeAirWidgetState>>(emptyList()) }

    suspend fun reload() {
        widgetStates = loadWidgetPreviews(context)
    }

    LaunchedEffect(Unit) { reload() }

    // Widgets can be added/removed from the home screen while this Activity isn't visible --
    // refresh whenever the user comes back to it so the list doesn't go stale.
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch { reload() }
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
            items(widgetStates) { state ->
                WidgetPreview(state = state, modifier = Modifier.fillMaxWidth())
            }
            item {
                AddWidgetCard(modifier = Modifier.fillMaxWidth())
            }
        }
    }
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
