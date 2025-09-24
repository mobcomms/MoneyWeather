package com.moneyweather.data.remote.response

data class UserPointResponse(
    val result: Int,
    val msg: String,
    val data: Data
) {
    data class Data(
        var currentPoint: Int,
        var todayAvailablePoints: Int,
        var todayEarnedClickPoints: Int
    )
}