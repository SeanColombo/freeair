package com.seancolombo.freeair.widget

/**
 * Which sensor a single placed widget instance shows. Deliberately separate from
 * [com.seancolombo.freeair.airquality.AirQualitySensorConfig], which also carries the API
 * key -- that key is app-global (one PurpleAir account), while the sensor is per-widget, so a
 * user could eventually place multiple widgets for different sensors.
 */
data class WidgetSensorConfig(val sensorId: String)
