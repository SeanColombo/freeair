package com.seancolombo.freeair.about

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.seancolombo.freeair.BuildConfig

private const val GITHUB_REPO_URL = "https://github.com/SeanColombo/freeair"
private const val GITHUB_ISSUES_URL = "https://github.com/SeanColombo/freeair/issues/new"
private const val LICENSE_URL = "https://github.com/SeanColombo/freeair/blob/main/LICENSE"
private const val PRIVACY_POLICY_URL = "https://github.com/SeanColombo/freeair/blob/main/PRIVACY.md"

/**
 * Shown from the main screen's hamburger menu -- app version plus links out to the project on
 * GitHub (repo, issues, license, privacy policy). No in-app content to keep in sync; everything
 * links out to the source of truth on GitHub.
 */
@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = "FreeAir", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Version ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Android widget for viewing PurpleAir air quality data. This project is not " +
                "affiliated with nor endorsed by PurpleAir. Made with love by Sean Colombo.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        AboutLinkRow(label = "GitHub repository", url = GITHUB_REPO_URL)
        AboutLinkRow(label = "Report an issue", url = GITHUB_ISSUES_URL)
        AboutLinkRow(label = "MIT License", url = LICENSE_URL)
        AboutLinkRow(label = "Privacy Policy", url = PRIVACY_POLICY_URL)
    }
}

@Composable
private fun AboutLinkRow(label: String, url: String) {
    val context = LocalContext.current
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
            .padding(vertical = 12.dp),
    )
}
