package com.moneyweather.data.remote.response

import com.moneyweather.model.Region
import com.moneyweather.model.Weather

data class WeatherResponse(val data: Data) {
    data class Data(
        var region: Region,
        var weathers: ArrayList<Weather>
    )

    fun getWeather(): Weather = data.weathers.takeIf { it.isNotEmpty() }?.get(0) ?: Weather()

    fun getRegion(): Region = data.region

    companion object {
        val EMPTY = WeatherResponse(Data(Region(), ArrayList()))
    }
}