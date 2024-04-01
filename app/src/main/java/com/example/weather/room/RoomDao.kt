package com.example.weather.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.weather.model.DateModel

@Dao
interface RoomDao {
    @Upsert
    suspend fun upsert(entity: DateModel)

    @Query("SELECT * FROM WeatherTable WHERE date = :date")
    fun getEntryById(date: String): DateModel?
}