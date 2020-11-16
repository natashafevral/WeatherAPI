package com.natasha.weatherapi

import android.app.Application

class WeatherApplication: Application() {

    companion object {
        lateinit var instance: WeatherApplication

    }
    var unitValue = "metric"
    val windMap: Map<Int, String> = mapOf(
        0 to "С",
        45 to "СВ",
        90 to "В",
        135 to "ЮВ",
        180 to "Ю",
        225 to "ЮЗ",
        270 to "З",
        315 to "СЗ",
        360 to "С"
    )

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}