package com.seancolombo.freeair.airquality.purpleair

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PurpleAirWidgetCodeParserTest {
    @Test
    fun `extracts the sensor index from a widget code snippet`() {
        val widgetHtml = """
            <div id="PurpleAirWidget_183609_averages" class="purpleAirWidget">
              <script src="https://www.purpleair.com/pa.widget.js"></script>
            </div>
        """.trimIndent()

        assertEquals(183609L, PurpleAirWidgetCodeParser.parseSensorIndex(widgetHtml))
    }

    @Test
    fun `extracts the sensor index from the real widget code, ignoring the average setting`() {
        // The "average_10_" fragment (a widget config setting, not the sensor ID) is also
        // underscore-wrapped -- the real ID must win since it appears first in both lines.
        val widgetHtml = """
            <div id='PurpleAirWidget_183609_module_US_EPA_AQI_conversion_C0_average_10_layer_US_EPA_AQI'>Loading PurpleAir Widget...</div>
            <script src='https://www.purpleair.com/pa.widget.js?key=D83RANBZQDMSILF8&module=US_EPA_AQI&conversion=C0&average=10&layer=US_EPA_AQI&container=PurpleAirWidget_183609_module_US_EPA_AQI_conversion_C0_average_10_layer_US_EPA_AQI'></script>
        """.trimIndent()

        assertEquals(183609L, PurpleAirWidgetCodeParser.parseSensorIndex(widgetHtml))
    }

    @Test
    fun `extracts the sensor index from just the widget's script URL`() {
        val scriptUrl = "https://www.purpleair.com/pa.widget.js?key=D83RANBZQDMSILF8&module=US_EPA_AQI" +
            "&container=PurpleAirWidget_183609_module_US_EPA_AQI_conversion_C0_average_10_layer_US_EPA_AQI"

        assertEquals(183609L, PurpleAirWidgetCodeParser.parseSensorIndex(scriptUrl))
    }

    @Test
    fun `extracts the sensor index from just the widget's div id`() {
        val widgetDivId = "PurpleAirWidget_183609_averages"
        assertEquals(183609L, PurpleAirWidgetCodeParser.parseSensorIndex(widgetDivId))
    }

    @Test
    fun `extracts the sensor index when it's just wrapped in underscores`() {
        assertEquals(183609L, PurpleAirWidgetCodeParser.parseSensorIndex("_183609_"))
    }

    @Test
    fun `returns null when there's no underscore-wrapped id`() {
        val widgetHtml = "<div>not a purple air widget</div>"

        assertNull(PurpleAirWidgetCodeParser.parseSensorIndex(widgetHtml))
    }
}
