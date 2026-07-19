package com.seancolombo.freeair.about

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
import com.seancolombo.freeair.ui.AppTopBar
import com.seancolombo.freeair.ui.theme.FreeAirTheme

/** Standalone entry point to [AboutScreen] from the main app's hamburger menu. */
class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreeAirTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { AppTopBar(onBackClick = { finish() }) },
                ) { innerPadding ->
                    AboutScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

fun buildAboutIntent(context: Context): Intent = Intent(context, AboutActivity::class.java)
