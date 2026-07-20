package com.seancolombo.freeair.widget.config

import com.seancolombo.freeair.widget.WidgetSensorConfig
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetConfigModelTest {
    @Test
    fun `starts with the initial sensor id`() {
        val model = WidgetConfigModel(initialSensorId = "12345", onSave = {})

        assertEquals("12345", model.sensorId)
    }

    @Test
    fun `a blank sensor id cannot be saved`() {
        val model = WidgetConfigModel(initialSensorId = "12345", onSave = {})

        model.onSensorIdChanged("   ")

        assertFalse(model.canSave)
    }

    @Test
    fun `save trims whitespace and calls through with the new config`() = runTest {
        var saved: WidgetSensorConfig? = null
        val model = WidgetConfigModel(initialSensorId = "12345", onSave = { saved = it })

        model.onSensorIdChanged("  67890  ")
        val result = model.save()

        assertTrue(result)
        assertEquals(WidgetSensorConfig("67890"), saved)
    }

    @Test
    fun `save does nothing and returns false when the sensor id is blank`() = runTest {
        var saveCalled = false
        val model = WidgetConfigModel(initialSensorId = "12345", onSave = { saveCalled = true })

        model.onSensorIdChanged("")
        val result = model.save()

        assertFalse(result)
        assertFalse(saveCalled)
    }

    @Test
    fun `pasting the widget code html silently cleans it down to just the sensor id`() {
        val model = WidgetConfigModel(initialSensorId = "", onSave = {})
        val widgetHtml = """
            <div id='PurpleAirWidget_183609_module_US_EPA_AQI_conversion_C0_average_10_layer_US_EPA_AQI'>Loading PurpleAir Widget...</div>
            <script src='https://www.purpleair.com/pa.widget.js?key=D83RANBZQDMSILF8&module=US_EPA_AQI&conversion=C0&average=10&layer=US_EPA_AQI&container=PurpleAirWidget_183609_module_US_EPA_AQI_conversion_C0_average_10_layer_US_EPA_AQI'></script>
        """.trimIndent()

        model.onSensorIdChanged(widgetHtml)

        assertEquals("183609", model.sensorId)
    }

    @Test
    fun `pasting just the widget's script url silently cleans it down to just the sensor id`() {
        val model = WidgetConfigModel(initialSensorId = "", onSave = {})

        model.onSensorIdChanged(
            "https://www.purpleair.com/pa.widget.js?container=PurpleAirWidget_183609_module_US_EPA_AQI",
        )

        assertEquals("183609", model.sensorId)
    }

    @Test
    fun `pasting the sensor id wrapped in underscores silently cleans it down to just the sensor id`() {
        val model = WidgetConfigModel(initialSensorId = "", onSave = {})

        model.onSensorIdChanged("_183609_")

        assertEquals("183609", model.sensorId)
    }

    @Test
    fun `typing a plain sensor id passes through unchanged`() {
        val model = WidgetConfigModel(initialSensorId = "", onSave = {})

        model.onSensorIdChanged("183609")

        assertEquals("183609", model.sensorId)
    }

    @Test
    fun `unrecognized input passes through unchanged so the field still shows what was typed`() {
        val model = WidgetConfigModel(initialSensorId = "", onSave = {})

        model.onSensorIdChanged("not a sensor id")

        assertEquals("not a sensor id", model.sensorId)
    }

    @Test
    fun `typing one keystroke at a time doesn't get truncated by an incidental mid-edit match`() {
        // "x_5_y", typed one character at a time -- "x_5_" completes an "_<digits>_" match
        // partway through, but since each step only grows the field by one character (not a
        // paste landing all at once), it must not get cleaned down to "5" before "y" is typed.
        val model = WidgetConfigModel(initialSensorId = "", onSave = {})

        "x_5_y".forEach { char -> model.onSensorIdChanged(model.sensorId + char) }

        assertEquals("x_5_y", model.sensorId)
    }
}
