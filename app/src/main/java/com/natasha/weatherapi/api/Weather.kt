package com.natasha.weatherapi.api

import com.google.gson.annotations.SerializedName

data class Weather (
    @SerializedName("description")
    var description: String = "",
    @SerializedName("icon")
    var icon: String = "01d",
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("main")
    var main: String = ""
)