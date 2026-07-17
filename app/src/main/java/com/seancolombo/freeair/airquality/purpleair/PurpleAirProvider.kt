package com.seancolombo.freeair.airquality.purpleair

import com.seancolombo.freeair.airquality.AirQualityProvider
import com.seancolombo.freeair.airquality.AirQualityReading
import com.seancolombo.freeair.airquality.AirQualitySensorConfig
import java.time.Instant

class PurpleAirProvider(
    private val httpClient: PurpleAirHttpClient = RealPurpleAirHttpClient(),
) : AirQualityProvider {
    override suspend fun fetchReading(config: AirQualitySensorConfig): Result<AirQualityReading> =
        runCatching {
            val json = httpClient.getSensorJson(config.sensorId, config.apiKey)
            val response = PurpleAirResponseParser.parse(json)
            AirQualityReading(
                sensorId = response.sensorIndex.toString(),
                sensorName = response.name,
                pm25 = response.pm25,
                temperatureFahrenheit = response.temperatureFahrenheit,
                humidityPercent = response.humidityPercent,
                lastUpdated = Instant.ofEpochSecond(response.lastSeenEpochSeconds),
            )
        }
}
