package data.remote.repository

import data.remote.api.ReversedGeocodingApi
import data.remote.api.WeatherApi
import data.remote.model.CurrentWeather
import data.remote.model.ReversedCountry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(private val weatherApi: WeatherApi,
                        private val reversedGeocodingApi: ReversedGeocodingApi) {

    suspend fun getCurrentWeather(apiKey: String,
                                  countryName: String,
                                  airQualityData: String): CurrentWeather {
        return withContext(Dispatchers.IO) {
            weatherApi.getCurrentWeather(apiKey, countryName, airQualityData)
        }.await()
    }

    suspend fun getReversGeocodingCountryName(latitude: String, longitude: String, format: String): ReversedCountry {
        return withContext(Dispatchers.IO) {
            reversedGeocodingApi.getCountryNameByCoordinates(latitude, longitude, format)
        }.await()
    }
}