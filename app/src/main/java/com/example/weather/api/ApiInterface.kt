package com.example.weather.api

import com.example.weather.model.Weather
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("/v1/archive")
    suspend fun getOldWeather(
        @Query("latitude")
        latitude: String,
        @Query("longitude")
        longitude: String,
        @Query("start_date")
        startDate: String,
        @Query("end_date")
        endDate: String,
        @Query("daily", encoded = true)
        daily: String = "weather_code,temperature_2m_max,temperature_2m_min,temperature_2m_mean,precipitation_sum,wind_speed_10m_max,wind_direction_10m_dominant",
        @Query("timezone")
        timezone: String = "auto"
    ) : Response<Weather>
}