package com.seancolombo.freeair.about

import android.content.Intent
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AboutActivityTest {
    @Test
    fun `launches and shows content`() {
        val intent = Intent(RuntimeEnvironment.getApplication(), AboutActivity::class.java)

        val activity = Robolectric.buildActivity(AboutActivity::class.java, intent).create().get()

        assertFalse(activity.isFinishing)
    }
}
