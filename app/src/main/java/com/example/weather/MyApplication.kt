package com.example.weather

import android.app.Application
import com.example.weather.api.ApiInterface
import com.example.weather.api.ApiUtilities
import com.example.weather.repository.WeatherRepository
import com.example.weather.room.DateDatabase

class MyApplication: Application() {
    lateinit var weatherRepository: WeatherRepository

    override fun onCreate() {
        super.onCreate()

        val apiInterface = ApiUtilities.getInstance().create(ApiInterface::class.java)
        val database = DateDatabase.getDatabase(applicationContext)

        weatherRepository = WeatherRepository(apiInterface, database, applicationContext)
    }
}