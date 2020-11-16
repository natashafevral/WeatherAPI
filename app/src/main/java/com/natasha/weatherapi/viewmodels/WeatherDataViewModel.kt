package com.natasha.weatherapi.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.natasha.weatherapi.WeatherRepository
import com.natasha.weatherapi.api.*

class WeatherDataViewModel(application: Application): AndroidViewModel(application) {
    private var weatherRepository: WeatherRepository
    var allWeatherLiveData: MutableLiveData<List<WeatherData>> = MutableLiveData()
    private var _currWeatherLiveData = MutableLiveData<List<WeatherData>>()

    var nextDaysWeatherLiveData: LiveData<List<WeatherData>>? = null
    init {
        weatherRepository = WeatherRepository(application)
    }

    fun getWeatherData(cityID: Int, units: String): LiveData<List<WeatherData>> {
        var data = weatherRepository.getWeatherData(cityID, units)
        allWeatherLiveData = data

        getCurrentWeatherLiveData()
        return data
    }

    fun getWeatherDataByLocation(lat: Double, lon: Double, units: String): LiveData<List<WeatherData>> {
        var data = weatherRepository.getWeatherDataByLocation(lat, lon, units)
        allWeatherLiveData = data

        getCurrentWeatherLiveData()
        return data
    }

    fun getWeatherDataByLocation(coord: Coord, units: String): LiveData<List<WeatherData>> {
        var data = weatherRepository.getWeatherDataByLocation(coord.lat, coord.lon, units)
        allWeatherLiveData = data
        getCurrentWeatherLiveData()
        return data
    }

    fun getCurrentWeatherLiveData(): LiveData<List<WeatherData>> {
        val data = weatherRepository.getCurrentWeatherLiveData()

        return data
    }
    fun getDaysWeatherLiveData(): LiveData<List<WeatherData>> {
        val data = weatherRepository.getNextDaysWeatherLiveData()
        nextDaysWeatherLiveData = data
        return data
    }

    fun getCityLiveData(): LiveData<City> {
        return weatherRepository.getCityLiveData()
    }
}