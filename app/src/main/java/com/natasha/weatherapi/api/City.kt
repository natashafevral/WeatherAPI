package com.natasha.weatherapi.api

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "selectedCities")
data class City(
    @SerializedName("coord")
    @Embedded
    var coord: Coord = Coord(),
    @SerializedName("country")
    var country: String = "",
    @SerializedName("id")
    @PrimaryKey
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