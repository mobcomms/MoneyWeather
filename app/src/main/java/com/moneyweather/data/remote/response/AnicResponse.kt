package com.moneyweather.data.remote.response

data class AnicResponse(var result: Int, var msg: String, var data: Data) {
    data class Data(var historyId: Int)
}