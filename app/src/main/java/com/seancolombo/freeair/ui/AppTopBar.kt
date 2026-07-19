package com.seancolombo.freeair.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.seancolombo.freeair.about.buildAboutIntent
import com.seancolombo.freeair.widget.config.buildApiKeySettingsIntent

/**
 * Shared top bar shown on every top-level screen. By default shows the hamburger menu; pass
 * [onBackClick] on a screen reached by navigating deeper (e.g. [com.seancolombo.freeair.about.AboutActivity])
 * to show a back arrow instead, so there's always exactly one way back up the stack.
 *
 * Plain-text glyphs rather than Material icons, matching how the rest of the app (including the
 * widget itself) uses emoji/text glyphs for iconography instead of pulling in an icons dependency.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(onBackClick: (() -> Unit)? = null) {
    TopAppBar(
        title = { Text("FreeAir") },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Text(text = "←", style = MaterialTheme.typography.titleLarge)
                }
            } else {
                HamburgerMenu()
            }
        },
    )
}

@Composable
private fun HamburgerMenu() {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { menuExpanded = true }) {
            Text(text = "☰", style = MaterialTheme.typography.titleLarge)
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            // Works whether or not a key has been saved yet -- re-entering it just
            // overwrites the old one. More menu items will land here over time.
            DropdownMenuItem(
                text = { Text("Update API key") },
                onClick = {
                    menuExpanded = false
                    context.startActivity(buildApiKeySettingsIntent(context))
                },
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("About") },
                onClick = {
                    menuExpanded = false
                    context.startActivity(buildAboutIntent(context))
                },
            )
        }
    }
}
