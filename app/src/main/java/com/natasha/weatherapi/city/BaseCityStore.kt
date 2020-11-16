package com.natasha.weatherapi.city

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BaseCityStore {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCities(cities: List<BaseCity>)

    @Query("select * from baseCity order by rusName")
    fun getAllCities(): LiveData<List<BaseCity>>

    @Query("select * from baseCity order by :value")
    fun getAllCitiesOrderBy(value: String): LiveData<List<BaseCity>>

    @Query("select * from baseCity order by population DESC")
    fun getAllCitiesOrderByDesc(): LiveData<List<BaseCity>> //value: String

    @Query("select * from baseCity order by population DESC LIMIT :limit")
    fun getPopularCities(limit: Int): LiveData<List<BaseCity>>

    @Query("select * from baseCity where rusName Like :value||'%'  order by rusName")
    fun getCitiesStartWith(value: String?): LiveData<List<BaseCity>>

    @Query("select * from baseCity where id = :id")
    fun getCityByID(id: Int): LiveData<BaseCity>

    @Query("select id, name, rusName, state, country, population, lat, lon from (select ((ABS(lat-:lat) + ABS(lon-:lon)) / 2) as avgDivLocation, * from baseCity where ABS(lat-:lat) <= :divergence AND ABS(lon-:lon) <= :divergence group by avgDivLocation  order by avgDivLocation LIMIT 1)")
    fun getCityByCoord(lat: Double, lon: Double, divergence: Double): LiveData<BaseCity?>
}