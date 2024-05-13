package com.example.weather.model

data class Daily(
    val time: List<String>,
    val weather_code:List<Int>,
    val temperature_2m_max: List<Double?>,
    val temperature_2m_min: List<Double?>,
    val temperature_2m_mean: List<Double?>,
    val precipitation_sum: List<Double?>,
    val wind_speed_10m_max: List<Double?>,
    val wind_direction_10m_dominant: List<Int?>,
)