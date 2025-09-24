package com.moneyweather.model

class Location {
    var lat: String? = "0"
    var lon: String? = "0"


    override fun toString(): String {
        return "Weather(lat=$lat, lon=$lon)"
    }
}