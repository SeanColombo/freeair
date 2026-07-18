package com.seancolombo.freeair.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Same size/shape as [WidgetPreview] so it reads as "this is where a widget would go," with a
 * "+" badge in place of an AQI number. Shown both in the empty state and appended after any
 * existing widgets, so there's always an obvious way to add another.
 */
@Composable
fun AddWidgetCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val supported = remember { WidgetPinner.isSupported(context) }

    OutlinedCard(
        modifier = if (supported) {
            modifier.clickable { WidgetPinner.requestPin(context) }
        } else {
            modifier
        },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Add widget", fontWeight = FontWeight.Medium)
                Text(
                    text = if (supported) {
                        "Tap to add it to your home screen"
                    } else {
                        "Long-press your home screen and search for FreeAir to add it"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
