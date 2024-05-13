package com.example.weather.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "WeatherTable")
data class DateModel (
    @PrimaryKey()
    val date: String,
    val minTemp: Double,
    val maxTemp: Double,
    val meanTemp: Double,
    val weatherCode: Int,
    val precipitation: Double,
    val windSpeed: Double,
    val windDirection: Int,
)