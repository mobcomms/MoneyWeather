package com.moneyweather.data.remote.response

data class BaseStringResponse(var code: Int, var message: String, var data: Data) {
    data class Data(
        var token: String,
        var result: String,
        var results: ArrayList<String>,
        var confirmCode: String
    )
}