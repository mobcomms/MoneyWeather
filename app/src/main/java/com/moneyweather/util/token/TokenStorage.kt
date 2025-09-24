package com.moneyweather.util.token

import com.moneyweather.util.PrefRepository

object TokenStorage {

    fun getAccessToken(): String? {
        return PrefRepository.UserInfo.accessToken.takeIf { it.isNotEmpty() }
    }

    fun saveAccessToken(token: String) {
        PrefRepository.UserInfo.accessToken = token
    }

    fun getRefreshToken(): String? {
        return PrefRepository.UserInfo.refreshToken.takeIf { it.isNotEmpty() }
    }

    fun saveRefreshToken(token: String) {
        PrefRepository.UserInfo.refreshToken = token
    }

    fun clearTokens() {
        PrefRepository.UserInfo.accessToken = ""
        PrefRepository.UserInfo.refreshToken = ""
    }
}
