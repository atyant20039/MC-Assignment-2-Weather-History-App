package com.example.weather.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "WeatherTable")
data class DateModel (
    @PrimaryKey()
    val date: String,
    val minTemp: Double,
    val maxTemp: Double
)