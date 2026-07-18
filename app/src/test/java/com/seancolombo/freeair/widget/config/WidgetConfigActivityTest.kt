package com.seancolombo.freeair.widget.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Regression coverage for the entry-point contract: both the launcher's auto-launch after
 * placement and MainActivity's tap-to-edit rely on this activity correctly reading (or
 * rejecting) EXTRA_APPWIDGET_ID.
 */
@RunWith(RobolectricTestRunner::class)
class WidgetConfigActivityTest {
    @Test
    fun `a missing appWidgetId finishes immediately instead of showing a broken screen`() {
        val intent = Intent(RuntimeEnvironment.getApplication(), WidgetConfigActivity::class.java)

        val activity = Robolectric.buildActivity(WidgetConfigActivity::class.java, intent).create().get()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun `an invalid appWidgetId finishes immediately instead of showing a broken screen`() {
        val intent = Intent(RuntimeEnvironment.getApplication(), WidgetConfigActivity::class.java)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        val activity = Robolectric.buildActivity(WidgetConfigActivity::class.java, intent).create().get()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun `a valid appWidgetId does not finish immediately`() {
        val intent = Intent(RuntimeEnvironment.getApplication(), WidgetConfigActivity::class.java)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 123)

        val activity = Robolectric.buildActivity(WidgetConfigActivity::class.java, intent).create().get()

        assertFalse(activity.isFinishing)
    }
}
