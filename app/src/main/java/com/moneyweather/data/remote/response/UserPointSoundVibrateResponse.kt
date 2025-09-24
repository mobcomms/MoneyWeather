package com.moneyweather.data.remote.response

data class UserPointSoundVibrateResponse(
    val data: Data
) {
    data class Data(
        val useSound: Boolean,
        val useVibrate: Boolean,
    )
}