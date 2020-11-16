package com.natasha.weatherapi.api

import com.google.gson.annotations.SerializedName

data class Sys(
    @SerializedName("pod")
    var pod: String = ""
)