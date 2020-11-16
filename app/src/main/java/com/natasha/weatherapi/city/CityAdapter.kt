package com.natasha.weatherapi.city

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.natasha.weatherapi.R
import kotlinx.android.synthetic.main.item_city.view.*
import java.lang.StringBuilder

class CityAdapter(private var cityList: MutableList<BaseCity>, private var parentContext: Context): RecyclerView.Adapter<CityAdapter.CityViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_city, parent, false)
        Log.d("City adapter", "city size ${cityList.size}")
        return CityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.bind(cityList[position], position)
    }

    override fun getItemCount(): Int {
        return cityList.size
    }

    fun swipeData(list: List<BaseCity>) {
        cityList.clear()
        cityList.addAll(list)
        //Log.d("City adapter", "swipeData list size ${list.size}")
        //Log.d("City adapter", "swipeData cityList size ${cityList.size}")
        notifyDataSetChanged()
    }
    inner class CityViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(city: BaseCity, position: Int) {
            val cityID = city.id
            val fullCityName = StringBuilder(city.rusName)
            if (!city.state.isEmpty()) {
                fullCityName.append(", ")
                fullCityName.append(city.state)
            }
            itemView.city_name.text = fullCityName
            itemView.setOnClickListener {
                Log.d("City adapter", "isGetWeatherByCityID bef ${BaseCityViewModel.getIsGetWeatherByCityID()}")
                BaseCityViewModel.setIsGetWeatherByCityID(cityID != 0)
                Log.d("City adapter", "isGetWeatherByCityID after ${BaseCityViewModel.getIsGetWeatherByCityID()}")
                Log.d("city adapter", "get position elem: $position")

                val activity = (parentContext as CityActivity)
                val bundle = Bundle()
                bundle.putInt("cityID", cityID)
                Log.d("city Adapter", "submit cityID ${cityID}")
                val intent = Intent()
                intent.putExtras(bundle)
                activity.setResult(Activity.RESULT_OK, intent)
                activity.finish()
            }
        }
    }
}

