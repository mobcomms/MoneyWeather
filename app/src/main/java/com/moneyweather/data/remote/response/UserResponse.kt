package com.moneyweather.data.remote.response

import com.moneyweather.model.User

data class UserResponse(var result: Int, var msg: String, var data: User) {

}