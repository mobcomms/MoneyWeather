package com.moneyweather.data.remote.response


data class BaseResponse(var result: Int, var msg: String, var data: Data) {
    data class Data(
        var token: String,
        var result: Any,
        var path: String,
        var results: List<Any>,
        var confirmCode: String
    )
}