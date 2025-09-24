package com.moneyweather.data.remote.response

data class FindEmailResponse(var result: Int, var msg : String, var data : Data){
    data class Data(
        var maskedEmail: String
    )
}