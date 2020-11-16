package com.natasha.weatherapi.api

import com.google.gson.annotations.SerializedName

data class Wind(
    @SerializedName("deg")
    var deg: Int = 0,
    @SerializedName("speed")
    var speed: Double = 0.0
)