package com.moneyweather.util

import com.moneyweather.model.Weather
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object WeatherUtils {

    /**
     * 일출-일몰 여부 반환
     * @param weather
     * @return
     */
    fun checkSunrise(weather: Weather): Boolean {
        val timeFormat = SimpleDateFormat("HHmm", Locale.getDefault())
        val currentTime = timeFormat.parse(timeFormat.format(Date())).time
        val sunriseTime = timeFormat.parse(weather.sunrise).time
        val sunsetTime = timeFormat.parse(weather.sunset).time

        return when {
            currentTime in sunriseTime..sunsetTime -> true
            else -> false
        }
    }

    /**
     * 맑은 날 노을 노출 정책 : 일몰 30분 전~일몰 30분 이후 (일몰 1시간전 노출)
     * @param weather
     * @return
     */
    fun isSunsetGlow(weather: Weather): Boolean {
        val timeFormat = SimpleDateFormat("HHmm", Locale.getDefault())
        val currentTime = timeFormat.parse(timeFormat.format(Date())).time
        val sunsetTime = timeFormat.parse(weather.sunset).time
        val sunsetGlowInteval = 30 * 60 * 1000
        val sunsetGlowStartTime = sunsetTime - sunsetGlowInteval
        val sunsetGlowEndTime = sunsetTime + sunsetGlowInteval

        return when {
            currentTime in sunsetGlowStartTime..sunsetGlowEndTime -> true
            else -> false
        }
    }

    fun getDayOfWeek(): Int {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    }

    fun getDayOfWeekStr(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "일요일"
            Calendar.MONDAY -> "월요일"
            Calendar.TUESDAY -> "화요일"
            Calendar.WEDNESDAY -> "수요일"
            Calendar.THURSDAY -> "목요일"
            Calendar.FRIDAY -> "금요일"
            Calendar.SATURDAY -> "토요일"
            else -> ""
        }
    }
}