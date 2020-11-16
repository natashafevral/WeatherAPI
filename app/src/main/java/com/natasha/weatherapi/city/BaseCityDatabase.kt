package com.natasha.weatherapi.city

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.natasha.weatherapi.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


private const val DB_NAME = "basecities.db"

@Database(entities = [BaseCity::class], version = 1, exportSchema = false)
abstract class BaseCityDatabase: RoomDatabase() {

    abstract fun baseCityStore(): BaseCityStore

    //компаньон обджект вместо синглтона (т.к. класс абстракт)
    companion object {
        private var INSTANCE: BaseCityDatabase? = null

        fun getInstance(context: Context, coroutineScope: CoroutineScope, resources: Resources): BaseCityDatabase? {
            if (INSTANCE == null) {
                synchronized(BaseCityDatabase::class) {
                    val instance = Room.databaseBuilder(context.applicationContext, BaseCityDatabase::class.java, DB_NAME )
                        .addCallback(BaseCityDatabaseCallback(coroutineScope, resources))
                        .build()
                    INSTANCE = instance
                }
            }
            return INSTANCE
        }
    }

    private class BaseCityDatabaseCallback(private val coroutineScope: CoroutineScope, private val resources: Resources): RoomDatabase.Callback() {
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