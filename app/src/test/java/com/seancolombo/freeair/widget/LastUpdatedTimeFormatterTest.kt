package com.seancolombo.freeair.widget

import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LastUpdatedTimeFormatterTest {
    // Compares against numeric substrings rather than a hardcoded full string: the JDK's
    // AM/PM formatting uses a locale-specific space character (varies by JDK/CLDR version),
    // so asserting on the clock digits is what actually matters here.
    private val instant = Instant.parse("2026-07-17T19:05:00Z")

    @Test
    fun `converts to the given zone`() {
        val formatted = LastUpdatedTimeFormatter.format(instant, zoneId = ZoneId.of("America/Los_Angeles"), locale = Locale.US)

        assertTrue("expected 12:05 in <$formatted>", formatted.contains("12:05"))
    }

    @Test
    fun `a different zone produces a different local time`() {
        val la = LastUpdatedTimeFormatter.format(instant, zoneId = ZoneId.of("America/Los_Angeles"), locale = Locale.US)
        val utc = LastUpdatedTimeFormatter.format(instant, zoneId = ZoneId.of("UTC"), locale = Locale.US)

        assertTrue("expected 7:05 in <$utc>", utc.contains("7:05"))
        assertNotEquals(la, utc)
    }
}
