package com.moneyweather.data.remote.response

data class DailyAdPointResponse(
    var result: Int,
    var msg: String,
    val data: Data
) {
    data class Data(
        var isAvailableGameAd: Boolean,
        var isAvailableOfferWallAd: Boolean
    )
}