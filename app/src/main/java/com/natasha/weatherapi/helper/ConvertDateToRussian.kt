package com.natasha.weatherapi.helper


import android.util.Log
import java.lang.Exception
import java.text.SimpleDateFormat

import java.util.*

class ConvertDateToRussian(dateString: String, splitter: Char = '-') {
    private val dateSplit: List<String>?
    private var dateDay: String = "01"
    private var dateMonth: String = "01"
    private var dateYear: String = "2020"
    //private var locale: Locale = Locale("ru","RU", "RU")
    private val calendar: Calendar = Calendar.getInstance(Locale.GERMANY)

    private var formatter: SimpleDateFormat
    private  var date: Date?
    private var dateOfWeek = 0
    private var monthRus: String? = null
    init {
        formatter = SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY)
        date = try {
            formatter.parse(dateString)
        } catch (e: Exception) {
            Log.d("conver date torus", "fatal error")
            formatter.parse("2020-10-01")
        }
        if (date != null) {
            calendar.time = date!!
            dateOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            //Log.d("Covert date", "dateString ${dateString}, to date: ${date}, dateOfWeek: ${dateOfWeek} ")
        }
        dateSplit = dateString.split(splitter)
        dateYear = dateSplit[0]
        dateMonth = dateSplit[1]
        dateDay = dateSplit[2]

    }

    fun getMonthOnRussian(): String {
        if (monthRus == null) {
            monthRus = when (dateMonth) {
                "02" -> "фев."
                "03" -> "мар."
                "04" -> "апр."
                "05" -> "мая"
                "06" -> "июня"
                "07" -> "июля"
                "08" -> "авг."
                "09" -> "сен."
                "10" -> "окт."
                "11" -> "ноя."
                "12" -> "дек."
                else -> "янв."
            }
        }
        return monthRus!!
    }
    fun getDateOfWeekOnRussian(): String {
       return when (dateOfWeek) {
           2 -> "Пн"
           3 -> "Вт"
           4 -> "Ср"
           5 -> "Чт"
           6 -> "Пт"
           7 -> "Сб"
           else -> "Вс"
       }
    }
    fun getDay() = dateDay
}