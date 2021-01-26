package com.natasha.weatherapi.city

import android.content.Context
import android.content.res.Resources
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.natasha.weatherapi.R
import com.natasha.weatherapi.WeatherStore
import com.natasha.weatherapi.api.City
import com.natasha.weatherapi.api.WeatherResponse
import com.natasha.weatherapi.api.WeatherResponseShort

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


private const val DB_NAME = "weatherapi.db"

@Database(entities = [BaseCity::class, WeatherResponseShort::class, City::class], version = 1, exportSchema = false)
abstract class WeatherAPIDatabase: RoomDatabase() {

    abstract fun baseCityStore(): BaseCityStore

    abstract fun weatherStore(): WeatherStore

    //компаньон обджект вместо синглтона (т.к. класс абстракт)
    companion object {
        private var INSTANCE: WeatherAPIDatabase? = null

        fun getInstance(context: Context, coroutineScope: CoroutineScope, resources: Resources): WeatherAPIDatabase? {
            if (INSTANCE == null) {
                synchronized(WeatherAPIDatabase::class) {
                    val instance = Room.databaseBuilder(context.applicationContext, WeatherAPIDatabase::class.java, DB_NAME )
                        .addCallback(WeatherAPIDatabaseCallback(coroutineScope, resources))
                        .build()
                    INSTANCE = instance
                }
            }
            return INSTANCE
        }
    }

    private class WeatherAPIDatabaseCallback(private val coroutineScope: CoroutineScope, private val resources: Resources): RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                coroutineScope.launch {
                    val baseCityStore = database.baseCityStore()
                    prepareCityData(baseCityStore)
                }
            }
        }

        private suspend fun prepareCityData(baseCityStore: BaseCityStore) {

            val jsonCitiesString = resources.openRawResource(R.raw.citieslist).bufferedReader().use {
                it.readText()
            }

            val typeTokenCity = object : TypeToken<List<BaseCity>>() {}.type
            val citiesList = Gson().fromJson<List<BaseCity>>(jsonCitiesString, typeTokenCity)

            baseCityStore.insertAllCities(citiesList)
        }
    }


}