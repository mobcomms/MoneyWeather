package com.moneyweather.util.token

interface TokenProvider {

    fun getToken(): String

    fun refreshToken(): String?

    fun updateToken(newToken: String)

    fun clearTokens()
}