package com.moneyweather.data.remote.response

data class TokenResponse(var data: Data) {
    data class Data(
        var accessToken: String,
        var accessTokenExpireAt: String,
        var refreshToken: String,
        var refreshTokenExpireAt: String,
        var userId: Integer
    )
}