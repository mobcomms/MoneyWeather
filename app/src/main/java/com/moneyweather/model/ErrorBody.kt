package com.moneyweather.model

class ErrorBody {
    var errorCode: Int = 0
    var errorMessage: String? = "통신 중 에러가 발생했습니다."


    override fun toString(): String {
        return "ErrorBody(errorCode=$errorCode, errorMessage=$errorMessage)"
    }
}