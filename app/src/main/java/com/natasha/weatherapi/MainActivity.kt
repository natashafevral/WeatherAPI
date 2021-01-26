package com.natasha.weatherapi

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Animatable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

import android.os.Bundle
import android.os.Handler

import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.natasha.weatherapi.api.City
import com.natasha.weatherapi.api.Coord
import com.natasha.weatherapi.api.WeatherData
import com.natasha.weatherapi.api.Wind
import com.natasha.weatherapi.city.BaseCityViewModel
import com.natasha.weatherapi.city.CityActivity
import com.natasha.weatherapi.databinding.ActivityMainBinding
import com.natasha.weatherapi.viewmodels.StandardViewModelFactory
import com.natasha.weatherapi.viewmodels.WeatherDataViewModel
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var permissionRequestCode = 90
    private var isWait = false
    private var internetRequestCode = 0
    private lateinit var locationManager: LocationManager
    private var locationProvider = LocationManager.GPS_PROVIDER
    private var minTimeLocation: Long = 0 //ms
    private var minDistanceLocation: Float = 1000F //meters
    private var PERMISSION_REQUEST_CODE = 99
    private var currentCoordLoc: Coord = Coord()
    private var providerIsEnabled: Boolean = false
    private var cityID: Int = 524901 // 524901 Moscow,  2643743 London(+1) 2023468 irkutsk(8) 2453866 Mali Rep.(0) 536203 (Piter) 2177478 (Australian +8) 580491 Astrakhanskaya
    private var timezone: Int = 0
    private var detailWeatherData: List<WeatherData> = listOf()
    private val detailDataLimit = 12
    private var appInstance: WeatherApplication
    private val viewModelFactory: StandardViewModelFactory //edit
    private lateinit var appName: String
    private lateinit var detailAdapter: DetailWeatherAdapter
    private var detailLayoutManager = LinearLayoutManager(this)

    private lateinit var dailyAdapter: DailyWeatherAdapter
    private var dailyLayoutManager = LinearLayoutManager(this)

    private var progressHumidityValue = 0
    private var startHumidityAnimFlag: Boolean = true
    private lateinit var progressHumidAnimator: ObjectAnimator
    init {
        appInstance = WeatherApplication.instance
        viewModelFactory = StandardViewModelFactory(appInstance)
    }

    private val weatherDataViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(WeatherDataViewModel::class.java)}

    /*private val baseCityViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(
        BaseCityViewModel::class.java
    )}*/


    private var locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val locCoord = Coord(46.0954386, 47.7006878)
            currentCoordLoc.lat = locCoord.lat //location.latitude
            currentCoordLoc.lon = locCoord.lon  // location.longitude
           /// 57.139836, 116.913522
            //46.0954386,47.7006878
                // 55.4489881,121.7776209 55.1565893,124.7196356
                //55.5373 89.1407 Шарыпово
                //56.338997 37.895396 Васильевское
            //Sergiev posad "lat": 56.299999 , lon = 38.133331
            //Sergiev posad "lat": 56.307165 , lon = 38.189537
            Log.d("main act geo", "lat: ${currentCoordLoc.lat}, lon: ${currentCoordLoc.lon}")
            providerIsEnabled = true
            getWeatherData()

          /* baseCityViewModel.getCityByCoord(currentCoordLoc, 0.5).observe(this@MainActivity, Observer {
               Log.d("main act geo city", "city: ${it}")
                /*if (it != null) {
                    cityID = it.id
                }
                getWeatherData(it != null)*/
            })*/
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String) {
            providerIsEnabled = true
            Log.d("main act geo", "provider enabled")
        }

        override fun onProviderDisabled(provider: String) {
            providerIsEnabled = false
            Log.d("main act geo", "provider disabled")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("main actn", "on Create")
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        appName = getString(R.string.app_name)
        binding.swipeRefresh.setOnRefreshListener {
            getWeatherData()
        }
        binding.icCity.setOnClickListener {
            val intent = Intent(this, CityActivity::class.java)
            intent.putExtra("CurrentCityID", cityID)
            startActivityForResult(intent, permissionRequestCode)
        }

        createProgressHumAnimator()

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("MAIN ACT RES", "requestCode $requestCode")
        if (requestCode == this.permissionRequestCode) {
            Log.d("main act result", "res code ${resultCode}")
            if (resultCode == RESULT_OK) {
                val extras = data?.extras
                val newCityID = extras?.getInt("cityID")
                newCityID?.let {
                    cityID = it
                    var byCityID = cityID != 0
                    Log.d("main actn", "onActivityResult get Weather Data")
                }
                Log.d("main activity", "newCityID ${newCityID}")
            }
        } else if (requestCode == internetRequestCode) {
            isWait = true
            Log.d("main act result", "before ")
            Toast.makeText(this, "Ожидание сети...", Toast.LENGTH_SHORT).show()
            CoroutineScope(Dispatchers.IO).launch {
                delay(4000)
                withContext(Dispatchers.Main) {
                    isWait = false
                    getWeatherData()
                }
            }

            Log.d("main actn resume", "after ")

        }

    }

    override fun onResume() {
        super.onResume()

        Log.d("MAIN ACT", "ON RESUME SPSPSPS")
        if (!BaseCityViewModel.getIsGetWeatherByCityID() && !isWait ) {
            Log.d("main acti resume", "is not bycityID => by location")
            if (checkPermissionLocation()) {
                Log.d("main acti resume", "permission true")
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Log.d("main acti resume", "permission ACCESS_FINE_LOCATION granted")
                    locationManager.requestLocationUpdates(
                        locationProvider,
                        minTimeLocation,
                        minDistanceLocation,
                        locationListener
                    )
                } else {
                    Log.d("main acti resume", "permission ACCESS_FINE_LOCATION denied")
                    BaseCityViewModel.setIsGetWeatherByCityID(true)
                    getWeatherData()
                }
            } else {
                Log.d("main acti resume", "permission false")
                 Toast.makeText(this, "Разрешение отклонено", Toast.LENGTH_SHORT).show()
                BaseCityViewModel.setIsGetWeatherByCityID(true)
                getWeatherData()
            }
        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // пермишен получен можем работать с locationManager
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
                        locationManager.requestLocationUpdates(
                            locationProvider,
                            minTimeLocation,
                            minDistanceLocation,
                            locationListener
                        )
                    }
                } else {
                    Log.d("main act geo", "permission denied")
                    BaseCityViewModel.setIsGetWeatherByCityID(true)
                    getWeatherData()
                }
                return
            }
        }
    }

    private fun checkPermissionLocation(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("main Check Permisison", "is not granted to ACCESS_FINE_LOCATION")
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                Log.d("main Check Permisison", "is not granted to ACCESS_COARSE_LOCATION")
                AlertDialog.Builder(this)
                    .setTitle(R.string.title_permission_location)
                    .setMessage(R.string.message_permission_location)
                    .setPositiveButton(
                        R.string.continue_action,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(p0: DialogInterface?, p1: Int) {
                                ActivityCompat.requestPermissions(
                                    this@MainActivity,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    PERMISSION_REQUEST_CODE
                                )
                            }
                        }).setNegativeButton(R.string.cancel, null)
                    .create()
                    .show()

            } else {
                Log.d("main Check Permisison", "is granted to ACCESS_COARSE_LOCATION")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_CODE
                )
            }
            return false
        } else {
            Log.d("main Check Permisison", "is  granted to ACCESS_FINE_LOCATION")
            return true
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }
        return result
    }


    private fun getWeatherData() {
        if (isInternetAvailable(this.applicationContext)) {
            Log.d("main act get W", "is byCityID ${BaseCityViewModel.getIsGetWeatherByCityID()}")
            binding.swipeRefresh.isRefreshing = true
            if (BaseCityViewModel.getIsGetWeatherByCityID()) {
                Log.d("main act get W", "I call getWeather cityID ${cityID}")
                weatherDataViewModel.getWeatherData(cityID, appInstance.unitValue).observe(
                    this,
                    { weatherList ->
                        detailWeatherData = weatherList.take(detailDataLimit)
                        binding.swipeRefresh.isRefreshing = false
                    }
                )
            } else {
                Log.d("main act check X", "currentCoordLoc: $currentCoordLoc, providerIsEnabled: $providerIsEnabled")
                if (providerIsEnabled) {
                    Log.d("main act get W", "I call getWeather coord")
                    weatherDataViewModel.getWeatherDataByLocation(currentCoordLoc, appInstance.unitValue)
                        .observe(this@MainActivity, { weatherList ->
                            detailWeatherData = weatherList.take(detailDataLimit)
                        })
                } else {
                    AlertDialog.Builder(this)
                        .setMessage(R.string.provider_disabled)
                        .setPositiveButton(R.string.settings, object : DialogInterface.OnClickListener {
                            override fun onClick(p0: DialogInterface?, p1: Int) {
                                val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                this@MainActivity.startActivity(callGPSSettingIntent)
                            }
                        }).setNegativeButton(R.string.cancel, null)
                        .create()
                        .show()
                }
                binding.swipeRefresh.isRefreshing = false
            }

            weatherDataViewModel.getCityLiveData().observe(this, {city ->
                Log.d("main actn", "Get city Live Data VM ${city}")

                timezone = city.timezone

                if (!BaseCityViewModel.getIsGetWeatherByCityID()) {
                    cityID = 0
                    BaseCityViewModel.setCityByCurrLocation(city)
                }
                fillDetailWeatherData(detailWeatherData)
                fillCityData(city)
            })

            weatherDataViewModel.getDaysWeatherLiveData().observe(this, { weatherList ->
                fillDailyWeatherData(weatherList)
                //Log.d("weather five data", "${weatherList}")
            })

            weatherDataViewModel.getCurrentWeatherLiveData().observe(this, { weatherList ->
                fillCurrentWeather(weatherList)
                //Log.d("curr weather data", "${it}")
            })
             weatherDataViewModel.getWeatherResponse().observe(this, {
                 Log.d("Main DB WEATHErr","weather resp is null?: ${it == null}")
                 Log.d("Main DB WEATHEr","weather resp: $it")
             })
        } else {
            binding.swipeRefresh.isRefreshing = false
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.internet_disabled, appName))
                .setPositiveButton(R.string.permission_access, object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        val callInternetIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        this@MainActivity.startActivityForResult(callInternetIntent, internetRequestCode)
                    }
                }).setNegativeButton(R.string.cancel, null)
                .create()
                .show()
        }
    }


    private fun fillDetailWeatherData(list: List<WeatherData>) { // DETAIL
        if (binding.detailWeather.adapter == null) {
            detailAdapter = DetailWeatherAdapter(list.toMutableList(), timezone)
            detailLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
            binding.detailWeather.layoutManager = detailLayoutManager
            binding.detailWeather.adapter = detailAdapter
            detailAdapter.notifyDataSetChanged()
        }
        detailAdapter.refreshData(list, timezone)
    }


    private fun fillDailyWeatherData(list: List<WeatherData>) { // DAILY FIVE
        //Log.d("activity five day", "list : ${list}")
        if (binding.dailyWeather.adapter == null) {
            dailyAdapter = DailyWeatherAdapter(list.toMutableList())
            dailyLayoutManager.orientation = LinearLayoutManager.VERTICAL
            binding.dailyWeather.layoutManager = dailyLayoutManager
            binding.dailyWeather.adapter = dailyAdapter
            dailyAdapter.notifyDataSetChanged()
        }
        dailyAdapter.refreshData(list)
    }

    private fun fillCityData(city: City) {
        binding.cityValue.text = city.name
    }

    private fun fillCurrentWeather(list: List<WeatherData>) {
        val currData = list.first()
        val currMain = currData.main
        val currTemp = "${currMain.temp.roundToInt()}°"
        binding.currTemp.text = currTemp
        val descript = currData.weather.first().description
        val currDescr = Character.toUpperCase(descript[0]) + descript.substring(1)
        binding.currDescription.text = currDescr
        val currFeel = "${currMain.feels_like.roundToInt()}°"
        binding.currFeelsLike.text = currFeel
        fillCurrentWind(currData.wind)
        fillCurrentComfort(currData.pop, currMain.pressure, currMain.humidity)
    }

    private fun fillCurrentWind(wind: Wind) {
        val deg = wind.deg
        val direction =  appInstance.windMap.filterKeys{ it-25 <=deg && it+25 > deg }.map{ y -> y.value}.firstOrNull()
        //Log.d("fill curr wind act", "${direction}, speed: ${wind.speed}")
        if (direction != null) {
            binding.windDirectionValue.text = direction
        }
        binding.windSpeedValue.text = "${wind.speed}"
        startWindAnimation()
    }

    private fun fillCurrentComfort(pop: Double, pressure: Int, humidity: Int) {
        val popText = "%.0f".format(pop)
        binding.precipitationValue.text = popText
        binding.pressureValue.text = pressure.toString()
        progressHumidityValue = humidity
        val humidText = "$progressHumidityValue%"
        binding.humidityValue.text = humidText
        progressHumidAnimator.setIntValues(0, progressHumidityValue)

    }

    private fun startWindAnimation() {
       val drawable = binding.windIcon.drawable
        if(drawable is Animatable) {
           (drawable as Animatable).start()
       }
    }

    private fun createProgressHumAnimator() {
        progressHumidAnimator = ObjectAnimator.ofInt(binding.progressHumidity, "progress", 0, progressHumidityValue)

        progressHumidAnimator.interpolator = FastOutSlowInInterpolator()
        progressHumidAnimator.duration = 4000

        binding.scrollView.setOnScrollChangeListener(object: View.OnScrollChangeListener {
            override fun onScrollChange(view: View?, x: Int, y: Int, oldx: Int, oldy: Int) {
                val scrollLineHeight = binding.scrollView.height
                val screenHeight = binding.scrollView.getChildAt(0).height
                val comfortLayoutPartHeight = binding.comfortLayout.height * 15 / 100
                val hh = y + scrollLineHeight
                val div = (screenHeight - comfortLayoutPartHeight)

                if (hh > div && startHumidityAnimFlag) {
                    Log.d("MAin ACT Scroll", "Change Scroll Pos  $y  $oldy; $hh > $div; ${progressHumidAnimator.values[0]}}")

                    progressHumidAnimator.start()
                    startHumidityAnimFlag = false
                }
                if (hh < (screenHeight - binding.comfortLayout.height) && !startHumidityAnimFlag) {
                    binding.progressHumidity.progress = 0
                    startHumidityAnimFlag = true

                }
            }
        })
    }





}