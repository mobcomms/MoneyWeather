package com.moneyweather.data.remote.response

data class VerificationResponse(var result: Int, var msg: String, var data: Data) {
    data class Data(
        var isVerified: Boolean
    )
}