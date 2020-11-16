package com.natasha.weatherapi

import com.natasha.weatherapi.api.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/2.5/forecast?&appid=aa75e64483a2e7b0bf59a63e004dabfc&lang=ru")
    fun getCurrentWeather(
        @Query("id")
        cityID: Int,
        @Query("units")
        units: String) : Call<WeatherResponse>

    @GET("data/2.5/forecast?&appid=aa75e64483a2e7b0bf59a63e004dabfc&lang=ru")
    fun getCurrentWeatherByLocation(
        @Query("lat")
        lat: Double,
        @Query("lon")
        lon: Double,
        @Query("units")
        units: String) : Call<WeatherResponse>
}