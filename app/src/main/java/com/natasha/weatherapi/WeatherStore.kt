package com.natasha.weatherapi

import androidx.lifecycle.LiveData
import androidx.room.*
import com.natasha.weatherapi.api.City
import com.natasha.weatherapi.api.Coord
import com.natasha.weatherapi.api.WeatherResponse
import com.natasha.weatherapi.api.WeatherResponseShort


@Dao
interface WeatherStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = WeatherResponseShort::class)
    suspend fun updateWeather(weather: WeatherResponseShort) //(@TypeConverters(WeatherResponseConverter::class) weather: WeatherResponse)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = City::class)
    suspend fun updateWeatherCity(city: City)

    @Query("select * from selectedCities")
    fun getWeatherCity(): LiveData<City?>

    @Query("select * from weatherResponse")
    fun getWeather(): LiveData<WeatherResponseShort?>

    @Query("select cityId from weatherResponse")
    fun getWeatherCityID(): LiveData<Int?>
}
