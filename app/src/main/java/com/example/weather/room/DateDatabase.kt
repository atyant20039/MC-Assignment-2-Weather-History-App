package com.example.weather.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weather.model.DateModel

@Database(entities = [DateModel::class], version = 1)
abstract class DateDatabase : RoomDatabase() {
    abstract fun dateDAO(): RoomDao

    companion object {
        private var INSTANCE : DateDatabase? = null

        fun getDatabase(context: Context) : DateDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context,DateDatabase::class.java, "WeatherDB").build()
            }

            return INSTANCE!!
        }
    }
}