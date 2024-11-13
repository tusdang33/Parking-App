package com.parking.parkingapp.data.repository

import com.parking.parkingapp.data.entity.mapbox.AutoCompleteMapboxResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApi {

    @GET("search/geocode/v6/forward")
    suspend fun getAutoCompleteAddress(
        @Query("q") query: String,
        @Query("country") country: String,
        @Query("proximity") proximity: String,
        @Query("autocomplete") autocomplete: Boolean,
        @Query("access_token") accessToken: String
    ): Response<AutoCompleteMapboxResponse>
}