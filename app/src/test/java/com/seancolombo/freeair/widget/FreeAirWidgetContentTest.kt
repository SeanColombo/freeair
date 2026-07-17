package com.seancolombo.freeair.widget

import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.hasText
import com.seancolombo.freeair.airquality.AqiCategory
import java.time.Instant
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FreeAirWidgetContentTest {
    private val lastUpdated = Instant.parse("2026-07-17T12:00:00Z")

    // Built the same way production code formats it, so this test doesn't depend on the
    // machine's timezone/locale to know what string to expect.
    private val formattedLastUpdated = LastUpdatedTimeFormatter.format(lastUpdated)

    @Test
    fun `loading state shows a loading message`() = runGlanceAppWidgetUnitTest {
        provideComposable { WidgetContent(FreeAirWidgetState.Loading) }

        onAllNodes(hasText("Loading…")).assertCountEquals(1)
    }

    @Test
    fun `error state shows a message and the underlying failure reason`() = runGlanceAppWidgetUnitTest {
        provideComposable { WidgetContent(FreeAirWidgetState.Error("boom")) }

        onAllNodes(hasText("Unable to load air quality")).assertCountEquals(1)
        onAllNodes(hasText("boom")).assertCountEquals(1)
    }

    @Test
    fun `loaded state shows the sensor name, AQI value, and last-updated time`() =
        runGlanceAppWidgetUnitTest {
            val state = FreeAirWidgetState.Loaded(
                sensorName = "Backyard Sensor",
                pm25Aqi = 71,
                category = AqiCategory.MODERATE,
                lastUpdated = lastUpdated,
            )

            provideComposable { WidgetContent(state) }

            onAllNodes(hasText("Backyard Sensor")).assertCountEquals(1)
            onAllNodes(hasText("71")).assertCountEquals(1)
            onAllNodes(hasText("Moderate · $formattedLastUpdated")).assertCountEquals(1)
        }

    @Test
    fun `a three-digit AQI (eg wildfire smoke) still renders correctly`() = runGlanceAppWidgetUnitTest {
        val state = FreeAirWidgetState.Loaded(
            sensorName = "Backyard Sensor",
            pm25Aqi = 295,
            category = AqiCategory.VERY_UNHEALTHY,
            lastUpdated = lastUpdated,
        )

        provideComposable { WidgetContent(state) }

        onAllNodes(hasText("295")).assertCountEquals(1)
        onAllNodes(hasText("Very Unhealthy · $formattedLastUpdated")).assertCountEquals(1)
    }
}
