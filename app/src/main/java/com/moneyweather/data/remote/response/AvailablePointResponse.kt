package com.moneyweather.data.remote.response

data class AvailablePointResponse(var result: Int, var msg: String, var data: Data) {
    data class Data(
        var currentPoint: Int,
        var currentAvailablePoint: Int
    )
}