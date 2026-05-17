package com.example.nammareshme.models

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val name: String
)

data class Main(
    val temp: Double,
    val humidity: Int,
    val pressure: Int
)

data class Weather(
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double
)
