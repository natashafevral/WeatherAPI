package com.natasha.weatherapi

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.natasha.weatherapi.api.*
import com.natasha.weatherapi.helper.RetrofitBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherRepository(private val weatherStore: WeatherStore) {
    private var answerLiveData = MutableLiveData<WeatherResponse>()
    private var weatherDataList: List<WeatherData> = ArrayList<WeatherData>()
    private var weatherLiveData: MutableLiveData<List<WeatherData>> = MutableLiveData()
    private var cityLiveData: MutableLiveData<City> = MutableLiveData()
        /* Transformations.map(answerLiveData) {
        Log.d("weather repo for city", "CITY: $it")
        it.city
    }*/
    private var currentWeatherLiveData: LiveData<List<WeatherData>> = Transformations.map(weatherLiveData) { list ->
        val date = list[0].dt_txt.split(' ')[0]
        list.filter { d -> d.dt_txt.split(' ')[0].equals(date) }
    }
    private var nextDaysWeatherLiveData: LiveData<List<WeatherData>> = Transformations.map(weatherLiveData) {list ->
        val date = list[0].dt_txt.split(' ')[0]
        val groupWe = list.groupBy { it.dt_txt.split(' ').get(0)}.toMutableMap()
        groupWe.remove(date)
        val tmax = groupWe.map { g -> g.value.maxByOrNull { gg -> gg.main.temp_max}?.main?.temp_max?:0.0 }
        val tmin = groupWe.map { g -> g.value.minByOrNull { gg -> gg.main.temp_min}?.main?.temp_min?:0.0 }
        val descrWeather = groupWe.map { g -> g.value.maxByOrNull { gg -> gg.weather[0].main }?.weather?.get(0) ?: Weather() }
        val winds = groupWe.map { g -> g.value.maxByOrNull { gg -> gg.wind.speed }?.wind?: Wind() }
        val pressures = groupWe.map { g -> g.value.maxByOrNull { gg -> gg.main.pressure}?.main?.pressure?:0 }
        val humidities = groupWe.map { g -> g.value.maxByOrNull { gg -> gg.main.humidity}?.main?.humidity?:0 }
        val pops = groupWe.map { g -> g.value.maxByOrNull { gg -> gg.pop}?.pop?:0.0}
        val clouds = groupWe.map { g -> g.value.maxByOrNull { gg -> gg.clouds.all}?.clouds?: Clouds() }
        val listWeatherData = mutableListOf<WeatherData>()
        for (i: Int in 0 until groupWe.size)  {
           // Log.d("repo for i", "${i}")
            val cloud =  clouds[i]
            val main = Main(pressure = pressures[i], humidity = humidities[i], temp_min = tmin[i], temp_max = tmax[i] )
            val weather = descrWeather[i]
            val wind = winds[i]
            val pop = pops[i]
            val dt = groupWe.keys.elementAt(i)
            val weatherData = WeatherData(clouds = cloud, dt_txt = dt, main = main, pop = pop, weather = listOf(weather), wind = wind)
            listWeatherData.add(weatherData)
        }
        Log.d("repo result list", "${listWeatherData}")
        listWeatherData
    }

    fun getWeatherData(cityID: Int = 1006984, units: String = "metric"): MutableLiveData<List<WeatherData>> { //1006984 London, metric - Celsius
        //Log.d("reposit check city", "cityID: ${cityID}")
        val service = RetrofitBuilder().getApiService()
        val call = service.getCurrentWeather(cityID, units)
        startWeatherCall(call)
        return weatherLiveData
    }

    fun getWeatherDataByLocation(lat: Double = 0.0, lon: Double = 0.0, units: String = "metric"): MutableLiveData<List<WeatherData>> {
        val service = RetrofitBuilder().getApiService()
        val call = service.getCurrentWeatherByLocation(lat, lon, units)
        startWeatherCall(call)
        return weatherLiveData
    }

    private fun startWeatherCall(call: Call<WeatherResponse>): Boolean {
        Log.d("get Weather call", "${call.request()}")
        var isSuccess = false
        call.enqueue( object: Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                Log.d("Weather answer service", "response ${response.code()}")
                if (response.code() == 200) {
                    val result = response.body()
                    if (result != null) {
                        answerLiveData.value = result
                        val responseShort = WeatherResponseShort(result.city.id, result.list)
                        GlobalScope.launch {
                            weatherStore.updateWeatherCity(result.city)
                            weatherStore.updateWeather(responseShort)
                        }

                        weatherDataList = result.list
                        weatherLiveData.value = weatherDataList
                        cityLiveData.value = result.city
                       // Log.d("REpo success", "${cityLiveData.value} not null")
                        isSuccess = true
                    }
                    Log.d("Response body", "${weatherDataList}")
                } else {
                    Log.d("Weather answer if not 200", "response ")
                }

                
                    //Log.d("Weather answer service 2", "response ${cityLiveData.value }")
            }
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.d("Weather answer service", "failure ${t}")
            }
        })
        return isSuccess
    }


    fun getNextDaysWeatherLiveData() = nextDaysWeatherLiveData
    fun getAnswerLiveData() = answerLiveData
    fun getCurrentWeatherLiveData() = currentWeatherLiveData
    fun getCityLiveData(): LiveData<City> {
       // Log.d("REpo get city", "${cityLiveData.value} not null")
        return cityLiveData
    }
    fun getWeatherResponse(): LiveData<WeatherResponseShort?> {
        return weatherStore.getWeather()
    }

    fun getWeatherCity(): LiveData<City?> {
       return weatherStore.getWeatherCity()
    }
}