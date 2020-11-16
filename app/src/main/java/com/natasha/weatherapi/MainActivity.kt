package com.natasha.weatherapi

import android.Manifest
import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Animatable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle

import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import com.natasha.weatherapi.viewmodels.StandardViewModelFactory
import com.natasha.weatherapi.viewmodels.WeatherDataViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private var requestCode = 90
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
            Log.d("main act geo", "status changed")
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
        setContentView(R.layout.activity_main)

        Log.d("main actn", "on Create")
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager


        swipe_refresh.setOnRefreshListener {
            getWeatherData()
        }

        ic_city.setOnClickListener {
            val intent = Intent(this, CityActivity::class.java)
            intent.putExtra("CurrentCityID", cityID)
            startActivityForResult(intent, requestCode)
        }

        createProgressHumAnimator()

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == this.requestCode) {
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
        }

    }

    override fun onResume() {
        super.onResume()
        if (!BaseCityViewModel.getIsGetWeatherByCityID() ) {
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
        } else {
            Log.d("main actn resume", "onResume here")
            getWeatherData()

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
                        R.string.permission_access,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(p0: DialogInterface?, p1: Int) {
                                ActivityCompat.requestPermissions(
                                    this@MainActivity,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    PERMISSION_REQUEST_CODE
                                )
                            }
                        }).setNegativeButton(R.string.permission_denied, null)
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


    private fun getWeatherData() {
        Log.d("main act get W", "is byCityID ${BaseCityViewModel.getIsGetWeatherByCityID()}")
        swipe_refresh.isRefreshing = true
        if (BaseCityViewModel.getIsGetWeatherByCityID()) {
            Log.d("main act get W", "I call getWeather cityID ${cityID}")
            weatherDataViewModel.getWeatherData(cityID, appInstance.unitValue).observe(
                this,
                { weatherList ->
                    detailWeatherData = weatherList.take(detailDataLimit)
                    swipe_refresh.isRefreshing = false
                })
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
            swipe_refresh.isRefreshing = false
        }

        weatherDataViewModel.getCityLiveData().observe(this, {
            Log.d("main actn", "Get city Live Data VM ${it}")
            timezone = it.timezone
            if (!BaseCityViewModel.getIsGetWeatherByCityID()) {
                cityID = 0
                BaseCityViewModel.setCityByCurrLocation(it)
            }
            fillDetailWeatherData(detailWeatherData)
            fillCityData(it)
        })

        weatherDataViewModel.getDaysWeatherLiveData().observe(this, { weatherList ->
            fillDailyWeatherData(weatherList)
            //Log.d("weather five data", "${weatherList}")
        })

        weatherDataViewModel.getCurrentWeatherLiveData().observe(this, { weatherList ->
            fillCurrentWeather(weatherList)
            //Log.d("curr weather data", "${it}")
        })

    }


    private fun fillDetailWeatherData(list: List<WeatherData>) { // DETAIL
        if (detail_weather.adapter == null) {
            detailAdapter = DetailWeatherAdapter(list.toMutableList(), timezone)
            detailLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
            detail_weather.layoutManager = detailLayoutManager
            detail_weather.adapter = detailAdapter
            detailAdapter.notifyDataSetChanged()
        }
        detailAdapter.refreshData(list, timezone)
    }


    private fun fillDailyWeatherData(list: List<WeatherData>) { // DAILY FIVE
        //Log.d("activity five day", "list : ${list}")
        if (daily_weather.adapter == null) {
            dailyAdapter = DailyWeatherAdapter(list.toMutableList())
            dailyLayoutManager.orientation = LinearLayoutManager.VERTICAL
            daily_weather.layoutManager = dailyLayoutManager
            daily_weather.adapter = dailyAdapter
            dailyAdapter.notifyDataSetChanged()
        }
        dailyAdapter.refreshData(list)
    }

    private fun fillCityData(city: City) {
        city_value.text = city.name
    }

    private fun fillCurrentWeather(list: List<WeatherData>) {
        val currData = list.first()
        val currMain = currData.main
        val currTemp = "${currMain.temp.roundToInt()}°"
        curr_temp.text = currTemp
        val descript = currData.weather.first().description
        val currDescr = Character.toUpperCase(descript[0]) + descript.substring(1)
        curr_description.text = currDescr
        val currFeel = "${currMain.feels_like.roundToInt()}°"
        curr_feels_like.text = currFeel
        fillCurrentWind(currData.wind)
        fillCurrentComfort(currData.pop, currMain.pressure, currMain.humidity)
    }

    private fun fillCurrentWind(wind: Wind) {
        val deg = wind.deg
        val direction =  appInstance.windMap.filterKeys{ it-25 <=deg && it+25 > deg }.map{ y -> y.value}.firstOrNull()
        //Log.d("fill curr wind act", "${direction}, speed: ${wind.speed}")
        if (direction != null) {
            wind_direction_value.text = direction
        }
        wind_speed_value.text = "${wind.speed}"
        startWindAnimation()
    }

    private fun fillCurrentComfort(pop: Double, pressure: Int, humidity: Int) {
        val popText = "%.0f".format(pop)
        precipitation_value.text = popText
        pressure_value.text = pressure.toString()
        progressHumidityValue = humidity
        val humidText = "$progressHumidityValue%"
        humidity_value.text = humidText
        progressHumidAnimator.setIntValues(0, progressHumidityValue)

    }

    private fun startWindAnimation() {
       val drawable = wind_icon.drawable
        if(drawable is Animatable) {
           (drawable as Animatable).start()
       }
    }

    private fun createProgressHumAnimator() {
        progressHumidAnimator = ObjectAnimator.ofInt(progress_humidity, "progress", 0, progressHumidityValue)

        progressHumidAnimator.interpolator = FastOutSlowInInterpolator()
        progressHumidAnimator.duration = 4000

        scroll_view.setOnScrollChangeListener(object: View.OnScrollChangeListener{
            override fun onScrollChange(view: View?, x: Int, y: Int, oldx: Int, oldy: Int) {
                val scrollLineHeight = scroll_view.height
                val screenHeight = scroll_view.getChildAt(0).height
                val comfortLayoutPartHeight = comfort_layout.height * 15 / 100
                val hh = y + scrollLineHeight
                val div = (screenHeight - comfortLayoutPartHeight)

                if (hh > div && startHumidityAnimFlag) {
                    Log.d("MAin ACT Scroll", "Change Scroll Pos  $y  $oldy; $hh > $div; ${progressHumidAnimator.values[0]}}")

                    progressHumidAnimator.start()
                    startHumidityAnimFlag = false
                }
                if (hh < (screenHeight - comfort_layout.height) && !startHumidityAnimFlag) {
                    progress_humidity.progress = 0
                    startHumidityAnimFlag = true

                }
            }
        })
    }





}