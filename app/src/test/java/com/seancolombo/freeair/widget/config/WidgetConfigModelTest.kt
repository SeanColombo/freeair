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
}
