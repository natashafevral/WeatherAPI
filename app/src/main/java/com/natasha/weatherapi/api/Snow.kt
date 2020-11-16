package com.natasha.weatherapi.api

import com.google.gson.annotations.SerializedName

data class Snow (
    @SerializedName("1h")
    var volume: Int = 0
)