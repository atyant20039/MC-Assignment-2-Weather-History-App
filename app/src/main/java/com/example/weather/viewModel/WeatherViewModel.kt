package com.example.weather.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    fun getData(date: String, latitude: String, longitude: String){
        viewModelScope.launch (Dispatchers.IO){ weatherRepository.getWeather(date, latitude, longitude) }
    }

    val weather: MutableLiveData<DateModel?>
        get() = weatherRepository.weather
}