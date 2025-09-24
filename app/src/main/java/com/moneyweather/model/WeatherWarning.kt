package com.moneyweather.model

import com.moneyweather.R
import com.moneyweather.base.BaseApplication

class WeatherWarning {

    var issuanceAt: String = ""
    var effectiveAt: String = ""
    var strongWind: String = ""
    var typhoon: String = ""
    var stormSurge: String = ""
    var heavyRain: String = ""
    var heavySnow: String = ""
    var dryConditions: String = ""
    var yellowDust: String = ""
    var tsunami: String = ""
    var coldWave: String = ""
    var heatWave: String = ""


    fun warningText() : String?{
      val result =  typhoon.plus(",").plus(stormSurge).plus(",").plus(heavyRain).plus(",").plus(heavySnow).plus(",").plus(dryConditions).plus(",").plus(yellowDust).plus(",").plus(tsunami).plus(",").plus(coldWave)
            .plus(",").plus(heatWave)

        return result
    }


    override fun toString(): String {
        return "WeatherWarning(issuanceAt=$issuanceAt, effectiveAt=$effectiveAt,strongWind=$strongWind)"
    }
}
