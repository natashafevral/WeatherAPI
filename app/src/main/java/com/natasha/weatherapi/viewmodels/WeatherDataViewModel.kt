package com.natasha.weatherapi.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.natasha.weatherapi.WeatherRepository
import com.natasha.weatherapi.api.*
import com.natasha.weatherapi.city.WeatherAPIDatabase
import kotlinx.coroutines.launch

class WeatherDataViewModel(application: Application): AndroidViewModel(application) {
    private var weatherRepository: WeatherRepository
    private var allWeatherLiveData: MutableLiveData<List<WeatherData>> = MutableLiveData()
    private var _currWeatherLiveData = MutableLiveData<List<WeatherData>>()

    var nextDaysWeatherLiveData: LiveData<List<WeatherData>>? = null
    init {
        val db = WeatherAPIDatabase.getInstance(application, viewModelScope, application.resources)
        val dao = db!!.weatherStore()
        weatherRepository = WeatherRepository(dao)
    }

    fun getWeatherData(cityID: Int, units: String): LiveData<List<WeatherData>> {
        val data = weatherRepository.getWeatherData(cityID, units)
        allWeatherLiveData = data

        getCurrentWeatherLiveData()
        return data
    }

    fun getWeatherCity(): LiveData<City?> {
        return weatherRepository.getWeatherCity()
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

    fun getWeatherResponse(): LiveData<WeatherResponseShort?> {
        return weatherRepository.getWeatherResponse()
    }

    fun getCityLiveData(): LiveData<City> {
        return weatherRepository.getCityLiveData()
    }
}