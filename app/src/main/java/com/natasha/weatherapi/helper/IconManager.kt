package com.natasha.weatherapi.helper

import com.natasha.weatherapi.R

fun getIconID(codeImage: String): Int {
    return when (codeImage) {
        "01d" -> R.drawable.ic_clear_day
        "01n" -> R.drawable.ic_clear_night
        "02d" -> R.drawable.ic_cloudy_day
        "02n" -> R.drawable.ic_cloudy_night
        "03d" -> R.drawable.ic_middle_cloudy
        "03n" -> R.drawable.ic_middle_cloudy
        "04d" -> R.drawable.ic_heavy_cloudy
        "04n" -> R.drawable.ic_heavy_cloudy
        "10d" -> R.drawable.ic_sun_rain
        "09n" -> R.drawable.ic_rain
        "09d" -> R.drawable.ic_rain
        "10n" -> R.drawable.ic_rain
        "11d" -> R.drawable.ic_storm
        "11n" -> R.drawable.ic_storm
        "13d" -> R.drawable.ic_snow
        "13n" -> R.drawable.ic_snow
        "50d" -> R.drawable.ic_mist
        "50n" -> R.drawable.ic_mist
        else -> R.drawable.ic_middle_cloudy
    }
}

fun getDayIconId(codeImage: String): Int {
    return when (codeImage) {
        "01d" -> R.drawable.ic_clear_day
        "01n" -> R.drawable.ic_clear_day
        "02d" -> R.drawable.ic_cloudy_day
        "02n" -> R.drawable.ic_cloudy_day
        "03d" -> R.drawable.ic_middle_cloudy
        "03n" -> R.drawable.ic_middle_cloudy
        "04d" -> R.drawable.ic_heavy_cloudy
        "04n" -> R.drawable.ic_heavy_cloudy
        "10d" -> R.drawable.ic_sun_rain
        "09n" -> R.drawable.ic_rain
        "09d" -> R.drawable.ic_rain
        "10n" -> R.drawable.ic_sun_rain
        "11d" -> R.drawable.ic_storm
        "11n" -> R.drawable.ic_storm
        "13d" -> R.drawable.ic_snow
        "13n" -> R.drawable.ic_snow
        "50d" -> R.drawable.ic_mist
        "50n" -> R.drawable.ic_mist
        else -> R.drawable.ic_middle_cloudy
    }
}
