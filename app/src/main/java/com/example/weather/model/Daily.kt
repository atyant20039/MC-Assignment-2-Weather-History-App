package com.example.weather.model

data class Daily(
    val time: List<String>,
    val temperature_2m_max: List<Double?>,
    val temperature_2m_min: List<Double?>
)