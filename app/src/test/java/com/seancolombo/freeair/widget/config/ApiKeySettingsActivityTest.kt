package com.seancolombo.freeair.widget.config

import android.content.Intent
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Unlike WidgetConfigActivity, this entry point never needs an appWidgetId -- it should launch
 * and show content with a completely bare intent, whether or not any widget or API key exists.
 */
@RunWith(RobolectricTestRunner::class)
class ApiKeySettingsActivityTest {
    @Test
    fun `launches and shows content without needing any extras`() {
        val intent = Intent(RuntimeEnvironment.getApplication(), ApiKeySettingsActivity::class.java)

        val activity = Robolectric.buildActivity(ApiKeySettingsActivity::class.java, intent).create().get()

        assertFalse(activity.isFinishing)
    }
}
