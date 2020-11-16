package com.natasha.weatherapi.api

import com.google.gson.annotations.SerializedName

data class WeatherData(
    @SerializedName("clouds")
    var clouds: Clouds = Clouds(),
    @SerializedName("dt")
    var dt: Int = 0,
    @SerializedName("dt_txt")
    var dt_txt: String = "",
    @SerializedName("main")
    var main: Main = Main(),
    @SerializedName("pop")
    var pop: Double = 0.0,
    @SerializedName("sys")
    var sys: Sys = Sys(),
    @SerializedName("visibility")
    var visibility: Int = 0,
    @SerializedName("weather")
    var weather: List<Weather> = ArrayList<Weather>(),
    @SerializedName("wind")
    var wind: Wind = Wind(),
    @SerializedName("snow")
    var snow: Snow = Snow(),
    @SerializedName("rain")
    var rain: Rain = Rain()
)