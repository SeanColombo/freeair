package com.seancolombo.freeair.airquality.purpleair

import org.junit.Assert.assertEquals
import org.junit.Test

class PurpleAirMapUrlBuilderTest {
    @Test
    fun `builds a select-and-zoom link to the sensor`() {
        val url = PurpleAirMapUrlBuilder.build(sensorId = "12345", latitude = 47.6062, longitude = -122.3321)

        assertEquals(
            "https://map.purpleair.com/1/l/m/i/mAQI/a10/p2592000/cC0?select=12345#14.0/47.6062/-122.3321",
            url,
        )
    }

    @Test
    fun `honors a custom zoom level`() {
        val url = PurpleAirMapUrlBuilder.build(sensorId = "12345", latitude = 47.6062, longitude = -122.3321, zoom = 10.0)

        assertEquals(
            "https://map.purpleair.com/1/l/m/i/mAQI/a10/p2592000/cC0?select=12345#10.0/47.6062/-122.3321",
            url,
        )
    }
}
