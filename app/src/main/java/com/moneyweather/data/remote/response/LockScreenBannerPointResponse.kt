package com.moneyweather.data.remote.response

data class LockScreenBannerPointResponse(val data: Data) {

    data class Data(
        val lockScreenBannerPointInfo: LockScreenBannerPointInfo
    )

    data class LockScreenBannerPointInfo(
        val isPointAvailable: Boolean,
        val point: Int
    )
}