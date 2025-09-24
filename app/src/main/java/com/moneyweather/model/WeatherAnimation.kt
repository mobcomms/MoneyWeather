package com.moneyweather.model

class WeatherAnimation {
    var animationDesc: Int? = 0
    var skyCode: String? = ""
    var skyDescription: Float = 0f



    override fun toString(): String {
        return "WeatherAnimation(animationDesc=$animationDesc, skyCode=$skyCode,skyDescription=$skyDescription)"

    }
}