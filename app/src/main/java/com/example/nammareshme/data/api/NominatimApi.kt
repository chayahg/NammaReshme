package com.example.nammareshme.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Headers

interface NominatimApi {
    @Headers("User-Agent: NammaReshmeApp")
    @GET("reverse?format=json&addressdetails=1")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): NominatimResponse
}

data class NominatimResponse(
    val display_name: String,
    val address: NominatimAddress
)

data class NominatimAddress(
    val village: String? = null,
    val town: String? = null,
    val city: String? = null,
    val county: String? = null,
    val state_district: String? = null,
    val state: String? = null,
    val postcode: String? = null,
    val country: String? = null
)
