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
    fun `returns null when the snippet has no widget id`() {
        val widgetHtml = "<div>not a purple air widget</div>"

        assertNull(PurpleAirWidgetCodeParser.parseSensorIndex(widgetHtml))
    }
}
