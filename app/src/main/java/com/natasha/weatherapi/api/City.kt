package com.natasha.weatherapi.api

import com.google.gson.annotations.SerializedName

data class City(
    @SerializedName("coord")
    var coord: Coord = Coord(),
    @SerializedName("country")
    var country: String = "",
    @SerializedName("id")
    var id: Int = 1006984,
    @SerializedName("name")
    var name: String = "London",
    @SerializedName("sunrise")
    var sunrise: Int = 0,
    @SerializedName("sunset")
    var sunset: Int = 0,
    @SerializedName("timezone")
    var timezone: Int = 0
)