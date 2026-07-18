package com.seancolombo.freeair.widget

import android.appwidget.AppWidgetManager
import com.seancolombo.freeair.widget.config.WidgetConfigActivity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/** Covers MainActivity's tap-to-edit and the widget's own tap-to-set-up action. */
@RunWith(RobolectricTestRunner::class)
class WidgetConfigIntentTest {
    @Test
    fun `targets WidgetConfigActivity with the given appWidgetId`() {
        val context = RuntimeEnvironment.getApplication()

        val intent = buildWidgetConfigIntent(context, appWidgetId = 42)

        assertEquals(WidgetConfigActivity::class.java.name, intent.component?.className)
        assertEquals(42, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1))
    }
}
