package com.seancolombo.freeair.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity as actionStartActivityForIntent
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
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

// Matches the PurpleAir app's own convention: a ring around an indoor sensor's AQI circle,
// since indoor readings aren't directly comparable to the outdoor air quality the category
// colors are calibrated for. Black reads clearly against the light AQI colors in light mode;
// a near-white ring keeps the same legibility against the widget's dark background at night.
private val indoorRingColor = androidx.glance.color.ColorProvider(day = Color(0xFF000000), night = Color(0xFFE0E0E0))

class FreeAirWidget(
    private val provider: AirQualityProvider = PurpleAirProvider(),
) : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    // Glance only re-runs this suspend function from scratch for a brand-new session; if a
    // session for this widget is already running (e.g. the config screen's Save arrives while
    // the widget's just-added session is still settling), updateAll() merely refreshes the
    // composition's currentState() rather than restarting us. So the fetch itself is driven
    // reactively from within the composition (see WidgetEntryPoint's produceState, keyed on the
    // sensor config) instead of once here, or a same-session config save would silently never
    // take effect until the next session (e.g. the 15-minute periodic refresh) started fresh.
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        provideContent {
            WidgetEntryPoint(id = id, appWidgetId = appWidgetId, provider = provider)
        }
    }
}

@Composable
private fun WidgetEntryPoint(id: GlanceId, appWidgetId: Int, provider: AirQualityProvider) {
    val prefs = currentState<Preferences>()
    val sensorConfig = prefs.toWidgetSensorConfig()

    if (sensorConfig == null) {
        // Never configured -- don't fetch at all, just point the user at setup. Avoids both
        // a wasted network call and silently showing some other widget's sensor as if it
        // were this one's.
        WidgetContent(FreeAirWidgetState.NeedsSetup(appWidgetId))
        return
    }

    val context = LocalContext.current
    val cachedReading = prefs.toCachedWidgetReading()
    // Shows whatever the other consumer of this widget's shared cache (the widget itself, or the
    // in-app preview) last found, including a cached failure -- so a 404/etc. doesn't strand the
    // other surface on "Loading" forever while this fetch is still in flight.
    val cachedError = prefs.toCachedWidgetError()
    val state by produceState(
        initialValue = cachedReading?.toLoadedState()
            ?: cachedError?.let { FreeAirWidgetState.Error(it.message, sensorConfig.sensorId, appWidgetId) }
            ?: FreeAirWidgetState.Loading,
        key1 = sensorConfig,
    ) {
        val config = AirQualitySensorConfig(
            apiKey = BuildConfig.PURPLEAIR_API_KEY,
            sensorId = sensorConfig.sensorId,
        )
        val fetchResult = provider.fetchReading(config)
        val outcome = FreeAirWidgetStateReducer.reduce(fetchResult, cachedReading, sensorConfig.sensorId, appWidgetId)

        val cacheToPersist = outcome.cacheToPersist
        if (cacheToPersist != null && cacheToPersist != cachedReading) {
            updateAppWidgetState(context, id) { it.putCachedWidgetReading(cacheToPersist) }
        }
        val errorToPersist = outcome.errorToPersist
        if (errorToPersist != null) {
            updateAppWidgetState(context, id) { it.putCachedWidgetError(errorToPersist) }
        }

        value = outcome.state
    }
    WidgetContent(state)
}

@Composable
internal fun WidgetContent(state: FreeAirWidgetState) {
    Box(
        modifier = GlanceModifier.fillMaxSize().background(backgroundColor).padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            is FreeAirWidgetState.Loading -> StatusText("Loading…")
            is FreeAirWidgetState.NeedsSetup -> NeedsSetupContent(state)
            is FreeAirWidgetState.Error -> ErrorContent(state)
            is FreeAirWidgetState.Loaded -> LoadedContent(state)
        }
    }
}

@Composable
private fun NeedsSetupContent(state: FreeAirWidgetState.NeedsSetup) {
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivityForIntent(buildWidgetConfigIntent(context, state.appWidgetId))),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "👉", style = TextStyle(fontSize = 22.sp))
        Spacer(modifier = GlanceModifier.width(10.dp))
        Column {
            Text(
                text = "Tap to set up",
                maxLines = 1,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primaryTextColor),
            )
            Text(
                text = "Choose a sensor to show",
                maxLines = 2,
                style = TextStyle(fontSize = 11.sp, color = secondaryTextColor),
            )
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
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivityForIntent(buildWidgetConfigIntent(context, state.appWidgetId))),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Unable to load air quality",
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primaryTextColor),
        )
        Text(
            text = WidgetErrorMessageFormatter.format(state.message, state.sensorId),
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
    // The outer box is always 48.dp, indoor or not, so a sensor being indoor doesn't shift the
    // rest of the row -- the ring reads as an inset border rather than a bigger badge.
    Box(
        modifier = GlanceModifier
            .size(48.dp)
            .background(if (state.isIndoor) indoorRingColor else state.category.backgroundColor.toColorProvider())
            .cornerRadius(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = GlanceModifier
                .size(if (state.isIndoor) 42.dp else 48.dp)
                .background(state.category.backgroundColor.toColorProvider())
                .cornerRadius(if (state.isIndoor) 21.dp else 24.dp),
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
}

private fun RgbColor.toColorProvider(): ColorProvider = ColorProvider(Color(red, green, blue))
