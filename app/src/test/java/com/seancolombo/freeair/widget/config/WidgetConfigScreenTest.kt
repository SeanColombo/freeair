package com.seancolombo.freeair.widget.config

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Some launchers re-fire WidgetPinner's successCallback more than once for the same pin request
 * (see WidgetConfigScreen's doc comment), redundantly relaunching WidgetConfigActivity for a
 * widget that's already configured. These cover both sides of the fix: the redundant relaunch
 * must be skipped, but that must never swallow a deliberate request to edit an existing widget
 * (MainActivity's tap-to-edit, or the widget's own "tap to set up" state) -- both always pass
 * isExplicitRequest = true via buildWidgetConfigIntent.
 */
class WidgetConfigScreenTest {
    @Test
    fun `a system-triggered relaunch of an already-configured widget is skipped`() {
        assertTrue(shouldSkipRedundantRelaunch(isExplicitRequest = false, currentSensorId = "183609"))
    }

    @Test
    fun `an explicit tap-to-edit request is never skipped, even if already configured`() {
        assertFalse(shouldSkipRedundantRelaunch(isExplicitRequest = true, currentSensorId = "183609"))
    }

    @Test
    fun `a system-triggered request for a widget that isn't configured yet isn't skipped either`() {
        // First-time setup (android:configure's own auto-launch, or the successCallback firing
        // for real the first time) must still show the form -- there's no config to redirect to.
        assertFalse(shouldSkipRedundantRelaunch(isExplicitRequest = false, currentSensorId = ""))
    }
}
