package com.natasha.weatherapi.city

import android.app.SearchManager
import android.content.Context
import android.os.Bundle

import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager

import com.natasha.weatherapi.R
import com.natasha.weatherapi.WeatherApplication
import com.natasha.weatherapi.databinding.ActivityCityBinding
import com.natasha.weatherapi.viewmodels.StandardViewModelFactory


class CityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCityBinding
    private var popularLimit = 30
    private lateinit var cityAdapter: CityAdapter
    private var cityList: MutableList<BaseCity> = mutableListOf()
    private  var popularList: MutableList<BaseCity> = mutableListOf()
    private val viewModelFactory: StandardViewModelFactory = StandardViewModelFactory(
        WeatherApplication.instance
    )
    private val baseCityViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(
        BaseCityViewModel::class.java
    )}



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.setSupportActionBar(binding.toolbar)
        this.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        this.supportActionBar!!.setDisplayShowHomeEnabled(true)

        Log.d("city activity", "fill adapter. city size ${cityList.size}")
        cityAdapter = CityAdapter(cityList, this@CityActivity)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.cityList.layoutManager = layoutManager
        binding.cityList.adapter = cityAdapter
        Log.d("city activity", "end adapter")


        val currentLocationCity = BaseCity(id = 0, rusName = "Текущее место")
        popularList.add(0, currentLocationCity)
        /*var mycityID = BaseCityViewModel.currentCityID
        BaseCityViewModel.getCityByCurrLocation().observe(this, { coord ->
            Log.d("city activity", "city VM  myLocCoord: ${coord} ")
            coord?.let {
                val currentLocationCity = BaseCity(
                    id = 0,
                    rusName = "Текущее место",
                    state = it.name,
                    coord = it.coord
                )
                popularList.add(0, currentLocationCity)
            }
        })*/



        baseCityViewModel.getPopularCities(popularLimit).observe(this, Observer { list ->
            popularList.addAll(list)
            updateCityListAdapter(popularList)
            Log.d("city activity", "fill adapter. city size ${this.cityList.size}")
        })



        /*baseCityViewModel.getCityByID(524901).observe(this, {
            Log.d("city activity", "${it}")
        })*/
    }


    private fun updateCityListAdapter(list: List<BaseCity>) {
        this.cityList.clear()
        this.cityList.addAll(list)
        this.cityAdapter.notifyDataSetChanged() // cityAdapter.swipeData(tempList)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search_city, menu)

        val searchMenuItem: MenuItem? = menu?.findItem(R.id.action_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView: SearchView? = searchMenuItem?.actionView as SearchView

        searchView?.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    Log.d("city activity", " submit query: $query")

                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    Log.d("city activity", " change query: $newText")
                    baseCityViewModel.getCitiesStartWith(newText).observe(
                        this@CityActivity,
                        { list ->
                            Log.d("city activity", " city list observe: ${list.size}")
                            updateCityListAdapter(list)
                        })
                    return true
                }
            })
            setOnCloseListener {
                Log.d("city activity", "close search View")
                updateCityListAdapter(popularList)
                false
            }

        }
        return super.onCreateOptionsMenu(menu)
    }

}