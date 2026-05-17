package com.example.nammareshme.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
    private const val OPENWEATHER_BASE_URL = "https://api.openweathermap.org/"

    val nominatimApi: NominatimApi by lazy {
        Retrofit.Builder()
            .baseUrl(NOMINATIM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApi::class.java)
    }

    val weatherApi: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(OPENWEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}
