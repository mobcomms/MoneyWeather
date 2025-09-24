package com.moneyweather.model

data class User(
    var email: String,
    var name: String,
    var phone: String,
    var userId: String                              
) {

    override fun toString(): String {
        return "User(email=$email name=$name phone=$phone userId=$userId)"
    }
}