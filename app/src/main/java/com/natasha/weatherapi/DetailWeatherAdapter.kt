package com.natasha.weatherapi

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.natasha.weatherapi.api.WeatherData
import com.natasha.weatherapi.helper.getIconID
import kotlinx.android.synthetic.main.item_detail_weather.view.*
import java.lang.NumberFormatException
import kotlin.math.roundToInt

class DetailWeatherAdapter(var items: MutableList<WeatherData>, private var timezone: Int = 0): RecyclerView.Adapter<DetailWeatherAdapter.DetailWeatherHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailWeatherHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_detail_weather, parent, false)
        return DetailWeatherHolder(view)
    }

    override fun onBindViewHolder(holder: DetailWeatherHolder, position: Int) {
        holder.bind(this.items[position])
    }

    override fun getItemCount(): Int {
        return this.items.size
    }

    fun refreshData(newList: List<WeatherData>, timezone: Int) {
        this.items.clear()
        this.items.addAll(newList)
        this.timezone = timezone
        notifyDataSetChanged()
    }

    inner class DetailWeatherHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(weatherData: WeatherData) {
            val time = weatherData.dt_txt.split(' ').get(1)
           // Log.d("Detail holder j", time)
            val editedTime = redactTime(time, timezone)
           // Log.d("Detail holder e", editedTime)
            itemView.hour.text = editedTime
            val tempInt = weatherData.main.temp.roundToInt()
            val temp = "$tempInt°"
            itemView.temp.text = temp

            val weatherID = weatherData.weather[0].icon
            val iconID = getIconID(weatherID)
            itemView.icon.setImageResource(iconID)

        }
        private fun redactTime(time: String, timezone: Int): String {
            val gmtHours = timezone / 3600
            val timeHours = time.split(':')[0]
            var editedTimeHours = 0
            try {
                editedTimeHours = timeHours.toInt()
            } catch (e: NumberFormatException) {
                Log.d("moderate time zone", "{e}")
            }
            editedTimeHours = (editedTimeHours + gmtHours) % 24
            return "%02d:00".format(editedTimeHours)
        }
    }
}
