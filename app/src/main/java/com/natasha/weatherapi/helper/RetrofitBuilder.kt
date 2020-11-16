package com.natasha.weatherapi.helper

import com.natasha.weatherapi.BuildConfig
import com.natasha.weatherapi.WeatherService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitBuilder {
    private var retrofit: Retrofit? = null
    fun getApiService(): WeatherService {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(WeatherService::class.java)
    }
}