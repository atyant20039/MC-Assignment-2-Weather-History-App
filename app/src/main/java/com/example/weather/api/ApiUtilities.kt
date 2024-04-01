package com.example.weather.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUtilities {
    fun getInstance() : Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://archive-api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create()).build()
    }
}