package com.natasha.weatherapi.api

import com.google.gson.annotations.SerializedName

data class Rain (
    @SerializedName("1h")
    var volume: Int = 0
)