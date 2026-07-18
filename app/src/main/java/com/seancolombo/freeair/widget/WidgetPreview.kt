package com.seancolombo.freeair.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seancolombo.freeair.airquality.RgbColor

/**
 * A normal-Compose mirror of the Glance widget's own layout (badge + name + category/time),
 * so the app can show "this is what your widget looks like." Glance and Compose UI are
 * different toolkits with no shared composables, so this duplicates ~30 lines of layout --
 * kept in sync by both reading the same [FreeAirWidgetState].
 */
@Composable
fun WidgetPreview(state: FreeAirWidgetState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.clickable(onClick = onClick)) {
        when (state) {
            is FreeAirWidgetState.Loading -> Text(
                text = "Loading…",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            is FreeAirWidgetState.NeedsSetup -> Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "👉")
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Tap to set up", fontWeight = FontWeight.Medium)
                    Text(
                        text = "Choose a sensor to show",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            is FreeAirWidgetState.Error -> Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Unable to load air quality", fontWeight = FontWeight.Medium)
                Text(
                    text = WidgetErrorMessageFormatter.format(state.message, state.sensorId),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            is FreeAirWidgetState.Loaded -> LoadedPreview(state)
        }
    }
}

@Composable
private fun LoadedPreview(state: FreeAirWidgetState.Loaded) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The outer box is always 48.dp, indoor or not, so a sensor being indoor doesn't shift
        // the rest of the row -- the ring reads as an inset border rather than a bigger badge.
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(if (state.isIndoor) indoorRingColor() else state.category.backgroundColor.toComposeColor(), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(if (state.isIndoor) 42.dp else 48.dp)
                    .background(state.category.backgroundColor.toComposeColor(), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.pm25Aqi.toString(),
                    color = state.category.contentColor.toComposeColor(),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = state.sensorName, fontWeight = FontWeight.Medium)
            Text(
                text = "${state.category.label} · ${LastUpdatedTimeFormatter.format(state.lastUpdated)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun RgbColor.toComposeColor(): Color = Color(red, green, blue)

// Matches the PurpleAir app's own convention: a ring around an indoor sensor's AQI circle,
// since indoor readings aren't directly comparable to the outdoor air quality the category
// colors are calibrated for. Black reads clearly against the light AQI colors in light mode;
// a near-white ring keeps the same legibility against the app's dark background at night.
@Composable
private fun indoorRingColor(): Color = if (isSystemInDarkTheme()) Color(0xFFE0E0E0) else Color(0xFF000000)
