package com.seancolombo.freeair.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity as actionStartActivityForIntent
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.seancolombo.freeair.BuildConfig
import com.seancolombo.freeair.MainActivity
import com.seancolombo.freeair.airquality.AirQualityProvider
import com.seancolombo.freeair.airquality.AirQualitySensorConfig
import com.seancolombo.freeair.airquality.RgbColor
import com.seancolombo.freeair.airquality.purpleair.PurpleAirProvider

private val backgroundColor = androidx.glance.color.ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF1C1B1F))
private val primaryTextColor = androidx.glance.color.ColorProvider(day = Color(0xFF1C1B1F), night = Color(0xFFE6E1E5))
private val secondaryTextColor = androidx.glance.color.ColorProvider(day = Color(0xFF49454F), night = Color(0xFFCAC4D0))

class FreeAirWidget(
    private val provider: AirQualityProvider = PurpleAirProvider(),
) : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = getAppWidgetState<Preferences>(context, id)
        val config = AirQualitySensorConfig(
            apiKey = BuildConfig.PURPLEAIR_API_KEY,
            sensorId = prefs.toWidgetSensorConfig().sensorId,
        )
        val fetchResult = provider.fetchReading(config)
        val cachedReading = prefs.toCachedWidgetReading()
        val outcome = FreeAirWidgetStateReducer.reduce(fetchResult, cachedReading)

        if (outcome.cacheToPersist != null && outcome.cacheToPersist != cachedReading) {
            updateAppWidgetState(context, id) { prefs -> prefs.putCachedWidgetReading(outcome.cacheToPersist) }
        }

        provideContent {
            WidgetContent(outcome.state)
        }
    }
}

@Composable
internal fun WidgetContent(state: FreeAirWidgetState) {
    Box(
        modifier = GlanceModifier.fillMaxSize().background(backgroundColor).padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            is FreeAirWidgetState.Loading -> StatusText("Loading…")
            is FreeAirWidgetState.Error -> ErrorContent(state)
            is FreeAirWidgetState.Loaded -> LoadedContent(state)
        }
    }
}

@Composable
private fun StatusText(text: String) {
    Text(
        text = text,
        style = TextStyle(fontSize = 14.sp, color = secondaryTextColor),
    )
}

@Composable
private fun ErrorContent(state: FreeAirWidgetState.Error) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Unable to load air quality",
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primaryTextColor),
        )
        Text(
            text = state.message,
            maxLines = 2,
            style = TextStyle(fontSize = 11.sp, color = secondaryTextColor),
        )
    }
}

@Composable
private fun LoadedContent(state: FreeAirWidgetState.Loaded) {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Everything except the settings icon opens PurpleAir's map, zoomed to this sensor.
        val readingModifier = GlanceModifier.defaultWeight().let { modifier ->
            if (state.mapUrl != null) {
                modifier.clickable(actionStartActivityForIntent(Intent(Intent.ACTION_VIEW, Uri.parse(state.mapUrl))))
            } else {
                modifier
            }
        }
        Row(modifier = readingModifier, verticalAlignment = Alignment.CenterVertically) {
            AqiBadge(state)
            Spacer(modifier = GlanceModifier.width(10.dp))
            Column(modifier = GlanceModifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.sensorName,
                    maxLines = 1,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primaryTextColor),
                )
                Text(
                    // Wraps onto its own line if "category · time" doesn't fit at minimum
                    // widget size, rather than truncating and hiding the time -- see maxLines.
                    text = "${state.category.label} · ${LastUpdatedTimeFormatter.format(state.lastUpdated)}",
                    maxLines = 2,
                    style = TextStyle(fontSize = 10.sp, color = secondaryTextColor),
                )
            }
        }
        SettingsButton()
    }
}

// Long-pressing a home screen widget is reserved by the launcher for move/resize/remove and
// can't be intercepted, so this is the way in to the app instead.
@Composable
private fun SettingsButton() {
    Text(
        text = "⚙",
        modifier = GlanceModifier
            .clickable(actionStartActivity<MainActivity>())
            .padding(start = 8.dp),
        style = TextStyle(fontSize = 14.sp, color = secondaryTextColor),
    )
}

@Composable
private fun AqiBadge(state: FreeAirWidgetState.Loaded) {
    Box(
        modifier = GlanceModifier
            .size(48.dp)
            .background(state.category.backgroundColor.toColorProvider())
            .cornerRadius(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = state.pm25Aqi.toString(),
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = state.category.contentColor.toColorProvider(),
            ),
        )
    }
}

private fun RgbColor.toColorProvider(): ColorProvider = ColorProvider(Color(red, green, blue))
