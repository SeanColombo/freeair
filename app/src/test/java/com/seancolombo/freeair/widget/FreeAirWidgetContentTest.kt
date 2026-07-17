package com.seancolombo.freeair.widget

import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.hasText
import com.seancolombo.freeair.airquality.AqiCategory
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FreeAirWidgetContentTest {
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
    fun `loaded state shows the sensor name, AQI value, and category label`() = runGlanceAppWidgetUnitTest {
        val state = FreeAirWidgetState.Loaded(
            sensorName = "Backyard Sensor",
            pm25Aqi = 71,
            category = AqiCategory.MODERATE,
        )

        provideComposable { WidgetContent(state) }

        onAllNodes(hasText("Backyard Sensor")).assertCountEquals(1)
        onAllNodes(hasText("71")).assertCountEquals(1)
        onAllNodes(hasText("Moderate")).assertCountEquals(1)
    }
}
