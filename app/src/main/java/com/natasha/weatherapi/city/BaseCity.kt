package com.natasha.weatherapi.city

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.natasha.weatherapi.api.Coord

@Entity(tableName = "baseCity")
data class BaseCity(
    @PrimaryKey
    var id: Int = 0,
    var name: String = "",
    var rusName: String = "",
    var state: String = "",
    var country: String = "",
    var population: Int = 0,
    @Embedded
    var coord: Coord = Coord()
    )
