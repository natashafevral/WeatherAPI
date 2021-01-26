package com.natasha.weatherapi.api

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

//FOR API
data class WeatherResponse(
    @SerializedName("city")
    var city: City = City(),
    @SerializedName("cnt")
    var cnt: Int = 0,
    @SerializedName("cod")
    var cod: String = "",
    @SerializedName("list")
    var list: ArrayList<WeatherData> = ArrayList<WeatherData>(),
    @SerializedName("message")
    var message: Double = 0.0
)

//FOR DATABASE
@Entity(tableName = "weatherResponse",
    foreignKeys = [ForeignKey(
        entity = City::class, parentColumns = arrayOf("id"), childColumns = arrayOf("cityId"), onDelete = ForeignKey.CASCADE)
    ])
data class WeatherResponseShort(
    @PrimaryKey
    var cityId: Int? = 0,
    @Embedded
    var list: ArrayList<WeatherData>? = ArrayList<WeatherData>()
)


