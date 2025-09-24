package com.moneyweather.data.remote.response

import com.moneyweather.model.WeatherAnimation

data class ConfigInitResponse(var result: Int, var msg: String, var data: Data) {
    data class Data(
        var animation: WeatherAnimation,
        var verificationUrl : String,
        var termsOfServiceUrl : String,
        var termsOfPrivacyUrl : String,
        var termsOfLocationUrl: String,
        var companyUrl : String,
        var privacyUrl : String,
        var inviteBannerImageUrl : String,
        var externalWeatherUrl : String,
        var externalWeatherSearchUrl: String
    )

}