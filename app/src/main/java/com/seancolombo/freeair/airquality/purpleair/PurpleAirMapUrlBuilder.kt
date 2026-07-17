package com.seancolombo.freeair.airquality.purpleair

/**
 * Builds a link to PurpleAir's public map, zoomed in on one sensor. Per PurpleAir's own docs,
 * `?select=` alone only highlights the sensor -- the `#zoom/lat/lng` hash is what actually
 * centers and zooms the map there, so both are required.
 */
object PurpleAirMapUrlBuilder {
    private const val DEFAULT_ZOOM = 14.0

    fun build(sensorId: String, latitude: Double, longitude: Double, zoom: Double = DEFAULT_ZOOM): String =
        "https://map.purpleair.com/1/l/m/i/mAQI/a10/p2592000/cC0?select=$sensorId#$zoom/$latitude/$longitude"
}
