package com.natasha.weatherapi.city

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.natasha.weatherapi.api.City
import com.natasha.weatherapi.api.Coord


class BaseCityViewModel(application: Application): AndroidViewModel(application) {
    private val repository: BaseCityRepository


    private var defaultDivergence: Double = 0.1
    private var mainCoord: Coord = Coord()

    companion object {
        private var cityByLocation: MutableLiveData<City?> = MutableLiveData()

        private var isGetWeatherByCityID = false
        fun setIsGetWeatherByCityID(byCity: Boolean) {
            Log.d("base city VM ", "is GetByID CHANGED")
            isGetWeatherByCityID = byCity
        }
        fun getIsGetWeatherByCityID(): Boolean {
            Log.d("base city VM ", "is GetByID GETTER")
            return isGetWeatherByCityID
        }

        fun setCityByCurrLocation(city: City) {
            Log.d("BASE CITY VM", "I AM CHanged from ${cityByLocation.value} to $city")
            cityByLocation.value = city
        }
        fun getCityByCurrLocation(): LiveData<City?> {
            return cityByLocation
        }
    }



    init {
        val db = WeatherAPIDatabase.getInstance(application, viewModelScope, application.resources)
        val dao = db!!.baseCityStore()
        repository = BaseCityRepository(dao)
    }

    fun getAllCities(): LiveData<List<BaseCity>> {
        return repository.getAllCities()
    }

    fun getCityByID(id: Int): LiveData<BaseCity> {
        return repository.getCityByID(id)
    }

    fun getAllCitiesOrderBy(value: OrderCityBy): LiveData<List<BaseCity>> {
        return repository.getAllCitiesOrderBy(value.value)
    }
    fun getAllCitiesOrderByDesc(value: OrderCityBy): LiveData<List<BaseCity>> {
        Log.d("city VM", "order by ${value} and ${value.value}")
        return repository.getAllCitiesOrderByDesc(value.value)
    }
    fun getPopularCities(limit: Int): LiveData<List<BaseCity>> {
        return repository.getPopularCities(limit)
    }


    fun getCitiesStartWith(value: String?): LiveData<List<BaseCity>> {
        return repository.getCitiesStartWith(value)
    }

    fun getCityByCoord(lat: Double, lon: Double, divergence: Double): LiveData<BaseCity?> {
        mainCoord = Coord(lat, lon)
        defaultDivergence = divergence
        return getCityByCoordinate()
    }

    fun getCityByCoord(coord: Coord, divergence: Double): LiveData<BaseCity?> { //LiveData<List<BaseCity>>
        mainCoord = coord
        defaultDivergence = divergence
        return getCityByCoordinate()
    }

    fun getCityByCoord(lat: Double, lon: Double): LiveData<BaseCity?> {
        mainCoord = Coord(lat, lon)
        return getCityByCoordinate()
    }

    fun getCityByCoord(coord: Coord): LiveData<BaseCity?> {
        mainCoord = coord
        return getCityByCoordinate()
    }
    private fun getCityByCoordinate(): LiveData<BaseCity?> {
        return repository.getCityByCoord(mainCoord.lat, mainCoord.lon, defaultDivergence)

    }

}