package com.moneyweather.data.remote.request

import com.google.gson.annotations.SerializedName

data class PointSoundVibrateRequest(
    @SerializedName("useSound")
    val useSound: Boolean,
    @SerializedName("useVibrate")
    val useVibrate: Boolean
)