package com.example.weather.api

import com.example.weather.model.Weather
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("/v1/archive")
    suspend fun getOldWeather(
        @Query("latitude")
        latitude: String = "28.70405920",
        @Query("longitude")
        longitude: String = "77.10249020",
        @Query("start_date")
        startDate: String,
        @Query("end_date")
        endDate: String,
        @Query("daily", encoded = true)
        daily: String = "temperature_2m_max,temperature_2m_min",
        @Query("timezone")
        timezone: String = "auto"
    ) : Response<Weather>
}