package com.seancolombo.freeair.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetErrorMessageFormatterTest {
    @Test
    fun `a PurpleAir NotFoundError response formats as a sensor-not-found message`() {
        val rawMessage = "PurpleAir API request failed with HTTP 404: " +
            "{ \"api_version\" : \"V1.2.2-1.1.45\", \"time_stamp\" : 1784394188, " +
            "\"error\" : \"NotFoundError\", \"description\" : \"Cannot find a sensor with the provided parameters.\" }"

        val formatted = WidgetErrorMessageFormatter.format(rawMessage, sensorId = "12345")

        assertEquals("Error: Sensor ID 12345 not found.", formatted)
    }

    @Test
    fun `an unrecognized error falls back to the raw message unchanged`() {
        val rawMessage = "PurpleAir API request failed with HTTP 500: internal server error"

        val formatted = WidgetErrorMessageFormatter.format(rawMessage, sensorId = "12345")

        assertEquals(rawMessage, formatted)
    }

    @Test
    fun `a plain network exception message also falls back to the raw message unchanged`() {
        val formatted = WidgetErrorMessageFormatter.format("Unable to resolve host", sensorId = "12345")

        assertEquals("Unable to resolve host", formatted)
    }
}
