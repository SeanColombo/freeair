package com.seancolombo.freeair.widget.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import com.seancolombo.freeair.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.xmlpull.v1.XmlPullParser

/**
 * Both entry points (the launcher's auto-launch after placement, and MainActivity's
 * tap-to-edit) depend on declarative manifest/XML wiring that the compiler can't check --
 * `android:configure` is just a string naming a class. If someone renames or moves
 * [WidgetConfigActivity] without updating that string, or drops the APPWIDGET_CONFIGURE
 * intent-filter, nothing here would fail to compile; only these tests would catch it.
 */
@RunWith(RobolectricTestRunner::class)
class WidgetConfigManifestWiringTest {
    @Test
    fun `APPWIDGET_CONFIGURE resolves to WidgetConfigActivity per the manifest`() {
        val context = RuntimeEnvironment.getApplication()
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).setPackage(context.packageName)

        val resolved = context.packageManager.resolveActivity(intent, 0)

        assertNotNull("No activity resolves APPWIDGET_CONFIGURE -- check the manifest intent-filter", resolved)
        assertEquals(WidgetConfigActivity::class.java.name, resolved!!.activityInfo.name)
    }

    @Test
    fun `the widget info XML's android configure attribute names WidgetConfigActivity`() {
        val context = RuntimeEnvironment.getApplication()
        val parser = context.resources.getXml(R.xml.free_air_widget_info)

        var configureAttribute: String? = null
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "appwidget-provider") {
                configureAttribute = parser.getAttributeValue(ANDROID_NAMESPACE, "configure")
            }
            parser.next()
        }

        assertNotNull("free_air_widget_info.xml has no android:configure attribute", configureAttribute)
        assertEquals(WidgetConfigActivity::class.java.name, configureAttribute)
    }

    private companion object {
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}
