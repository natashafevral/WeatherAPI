package com.natasha.weatherapi

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.natasha.weatherapi.api.WeatherData
import com.natasha.weatherapi.helper.ConvertDateToRussian
import com.natasha.weatherapi.helper.getDayIconId
import kotlinx.android.synthetic.main.item_daily_weather.view.*
import kotlinx.android.synthetic.main.item_daily_weather.view.icon
import kotlin.math.roundToInt

class DailyWeatherAdapter(var items: MutableList<WeatherData>): RecyclerView.Adapter<DailyWeatherHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyWeatherHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_daily_weather, parent, false)
        return DailyWeatherHolder(view)
    }

    override fun onBindViewHolder(holder: DailyWeatherHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
    fun refreshData(newData: List<WeatherData>) {
        items.clear()
        items.addAll(newData)
        notifyDataSetChanged()
    }
}

class DailyWeatherHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    fun bind(weatherData: WeatherData) {
        val date = weatherData.dt_txt
       // Log.d("daily holder", "${date}")
        //Log.d("daily holder", "${weatherData}")
        val converter = ConvertDateToRussian(date)
        val day = converter.getDay()
        val monthRus = converter.getMonthOnRussian()
        val dayOfWeekRus = converter.getDateOfWeekOnRussian()
        val fullDay = "${dayOfWeekRus}, ${day} ${monthRus}"
        itemView.day.text = fullDay
       // Log.d("daily holder ", "${weatherData.weather[0]}")
        val weatherID = weatherData.weather[0].icon
        val iconID = getDayIconId(weatherID)
        itemView.icon.setImageResource(iconID)
        val minTemp = weatherData.main.temp_min.roundToInt()

        val maxTemp = weatherData.main.temp_max.roundToInt()

        val fullTemp = "$maxTemp° / $minTemp°"
        Log.d("daily holder", fullTemp)
        itemView.max_min_temp.text = fullTemp

    }
}