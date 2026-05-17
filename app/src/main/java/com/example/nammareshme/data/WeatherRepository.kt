package com.example.nammareshme.data

import com.example.nammareshme.data.api.WeatherApiService
import com.example.nammareshme.models.WeatherResponse

class WeatherRepository(private val apiService: WeatherApiService) {
    private val apiKey = "87d5927c9d1d659dde3f04b97c49cfeb"

    suspend fun getWeather(lat: Double, lon: Double): Result<WeatherResponse> {
        return try {
            val response = apiService.getCurrentWeather(lat, lon, apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
