package com.natasha.weatherapi.api

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("city")
    var city: City = City(),
    @SerializedName("cnt")
    var cnt: Int = 0,
    @SerializedName("cod")
    var cod: String = "",
    @SerializedName("list")
    var list: List<WeatherData> = ArrayList<WeatherData>(),
    @SerializedName("message")
    var message: Double = 0.0
)