package com.seancolombo.freeair.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
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
import com.seancolombo.freeair.airquality.AirQualityProvider
import com.seancolombo.freeair.airquality.AirQualitySensorConfig
import com.seancolombo.freeair.airquality.RgbColor
import com.seancolombo.freeair.airquality.purpleair.PurpleAirProvider

private val backgroundColor = androidx.glance.color.ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF1C1B1F))
private val primaryTextColor = androidx.glance.color.ColorProvider(day = Color(0xFF1C1B1F), night = Color(0xFFE6E1E5))
private val secondaryTextColor = androidx.glance.color.ColorProvider(day = Color(0xFF49454F), night = Color(0xFFCAC4D0))

private val KEY_SENSOR_NAME = stringPreferencesKey("sensor_name")
private val KEY_PM25_AQI = intPreferencesKey("pm25_aqi")
private val KEY_LAST_UPDATED_EPOCH_SECONDS = longPreferencesKey("last_updated_epoch_seconds")

private fun Preferences.toCachedWidgetReading(): CachedWidgetReading? {
    val sensorName = this[KEY_SENSOR_NAME] ?: return null
    val pm25Aqi = this[KEY_PM25_AQI] ?: return null
    val lastUpdatedEpochSeconds = this[KEY_LAST_UPDATED_EPOCH_SECONDS] ?: return null
    return CachedWidgetReading(sensorName, pm25Aqi, lastUpdatedEpochSeconds)
}

private fun MutablePreferences.putCachedWidgetReading(cached: CachedWidgetReading) {
    this[KEY_SENSOR_NAME] = cached.sensorName
    this[KEY_PM25_AQI] = cached.pm25Aqi
    this[KEY_LAST_UPDATED_EPOCH_SECONDS] = cached.lastUpdatedEpochSeconds
}

class FreeAirWidget(
    private val provider: AirQualityProvider = PurpleAirProvider(),
) : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val config = AirQualitySensorConfig(
            apiKey = BuildConfig.PURPLEAIR_API_KEY,
            sensorId = BuildConfig.PURPLEAIR_SENSOR_ID,
        )
        val fetchResult = provider.fetchReading(config)
        val cachedReading = getAppWidgetState<Preferences>(context, id).toCachedWidgetReading()
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
        AqiBadge(state)
        Spacer(modifier = GlanceModifier.width(10.dp))
        Column(modifier = GlanceModifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = state.sensorName,
                maxLines = 1,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primaryTextColor),
            )
            Text(
                // Wraps onto its own line if "category · time" doesn't fit at minimum widget
                // size, rather than truncating and hiding the time -- see maxLines.
                text = "${state.category.label} · ${LastUpdatedTimeFormatter.format(state.lastUpdated)}",
                maxLines = 2,
                style = TextStyle(fontSize = 10.sp, color = secondaryTextColor),
            )
        }
    }
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
