package com.example.weather.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.model.DateModel
import com.example.weather.model.Weather
import com.example.weather.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeatherViewModel (private val weatherRepository: WeatherRepository) : ViewModel() {
    fun getData(date: String){
        viewModelScope.launch (Dispatchers.IO){ weatherRepository.getWeather(date) }
    }

    val weather: LiveData<DateModel>
        get() = weatherRepository.weather
}