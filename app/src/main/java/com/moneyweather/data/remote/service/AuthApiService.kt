package com.moneyweather.data.remote.service

import com.moneyweather.data.remote.request.RefreshTokenRequest
import com.moneyweather.data.remote.response.TokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApiService {

    @Headers("Content-Type: application/json")
    @POST("/api/v1/sign/in/refresh")
    fun refreshTokenSync(@Body refreshToken: RefreshTokenRequest): Call<TokenResponse>
}