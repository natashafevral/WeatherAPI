package com.natasha.weatherapi.city

import androidx.lifecycle.LiveData

class BaseCityRepository(private val baseCityStore: BaseCityStore) {

    fun getAllCities(): LiveData<List<BaseCity>> {
        return baseCityStore.getAllCities()
    }

    fun getCityByID(id: Int): LiveData<BaseCity> {
        return baseCityStore.getCityByID(id)
    }

    fun getAllCitiesOrderBy(value: String): LiveData<List<BaseCity>> {
        return baseCityStore.getAllCitiesOrderBy(value)
    }

    fun getAllCitiesOrderByDesc(value: String): LiveData<List<BaseCity>> {
        return baseCityStore.getAllCitiesOrderByDesc()
    }
    fun getPopularCities(limit: Int): LiveData<List<BaseCity>> {
        return baseCityStore.getPopularCities(limit)
    }

    fun getCitiesStartWith(value: String?): LiveData<List<BaseCity>> {
        return baseCityStore.getCitiesStartWith(value)
    }

    fun getCityByCoord(lat: Double, lon: Double, divergence: Double): LiveData<BaseCity?> {
        return baseCityStore.getCityByCoord(lat, lon, divergence)
    }
}