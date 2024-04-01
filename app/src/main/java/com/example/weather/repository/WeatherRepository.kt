package com.example.weather.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.weather.api.ApiInterface
import com.example.weather.model.DateModel
import com.example.weather.model.Weather
import com.example.weather.room.DateDatabase
import com.example.weather.utils.InternetUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeatherRepository(private val apiInterface: ApiInterface, private val dateDatabase: DateDatabase, private val context: Context) {
    private val weatherLiveData = MutableLiveData<DateModel>()
    val weather: LiveData<DateModel>
        get() = weatherLiveData

    suspend fun getWeather(date: String) {
        if (InternetUtil.isInternetAvailable(context)){
            val currentDate = Calendar.getInstance()
            currentDate.set(Calendar.HOUR_OF_DAY, 0)
            currentDate.set(Calendar.MINUTE, 0)
            currentDate.set(Calendar.SECOND, 0)
            currentDate.set(Calendar.MILLISECOND, 0)

            val enteredDate = Calendar.getInstance().apply {
                // Parse the entered date to set the Calendar instance
                val dateParts = date.split("-").map { it.toInt() }
                set(dateParts[0], dateParts[1] - 1, dateParts[2]) // Subtracting 1 from month as it's 0-based
                set(Calendar.HOUR_OF_DAY, 0) // Set time to midnight
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val daysDifference = ((currentDate.timeInMillis - enteredDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

            var dateEntry:DateModel? = null;

            if (daysDifference <= 5){
                var minSum:Double = 0.0;
                var maxSum:Double = 0.0;

                for (subYear in 1..10){
                    val tempDate = Calendar.getInstance().apply {
                        val dateParts = date.split("-").map { it.toInt() }

                        set(dateParts[0]-subYear, dateParts[1]-1, dateParts[2])
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    val tempDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(tempDate.time)

                    val result = apiInterface.getOldWeather(startDate = tempDateStr, endDate = tempDateStr)

                    val tempDateEntry = result.body()?.daily?.temperature_2m_min?.get(0)?.let {
                        result.body()!!.daily.temperature_2m_max[0]?.let { it1 ->
                            DateModel(
                                date = result.body()?.daily?.time?.get(0) ?: "",
                                minTemp = it,
                                maxTemp = it1
                            )
                        }
                    }

                    if (tempDateEntry != null){
                        dateDatabase.dateDAO().upsert(tempDateEntry)
                    }

                    minSum += result.body()?.daily?.temperature_2m_min?.get(0)?:0.0
                    maxSum += result.body()?.daily?.temperature_2m_max?.get(0)?:0.0
                }

                val finalMinTemp = minSum/10
                val finalMaxTemp = maxSum/10

                dateEntry = DateModel(
                    date = date,
                    minTemp = "%.1f".format(finalMinTemp).toDouble(),
                    maxTemp = "%.1f".format(finalMaxTemp).toDouble()
                )
            } else {
                val result = apiInterface.getOldWeather(startDate = date, endDate = date)

                dateEntry = result.body()?.daily?.temperature_2m_min?.get(0)?.let {
                    result.body()!!.daily.temperature_2m_max[0]?.let { it1 ->
                        DateModel(
                            date = result.body()?.daily?.time?.get(0) ?: "",
                            minTemp = it,
                            maxTemp = it1
                        )
                    }
                }
            }

            if (dateEntry != null) {
                dateDatabase.dateDAO().upsert(dateEntry)
                weatherLiveData.postValue(dateEntry)
            }
        } else {
            val dateEntry = dateDatabase.dateDAO().getEntryById(date)

            if (dateEntry != null){
                weatherLiveData.postValue(dateEntry)
            } else {
                weatherLiveData.postValue(
                    DateModel(
                        date = "",
                        minTemp = 0.0,
                        maxTemp = 0.0
                    )
                )
            }
        }

    }
}