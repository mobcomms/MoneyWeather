package com.moneyweather.util.token

import com.moneyweather.data.remote.request.RefreshTokenRequest
import com.moneyweather.data.remote.service.AuthApiService
import timber.log.Timber
import javax.inject.Inject

class TokenProviderImpl @Inject constructor(
    private val apiService: AuthApiService,
) : TokenProvider {

    companion object {
        val TAG: String = TokenProviderImpl::class.java.simpleName
    }

    override fun getToken(): String {
        return TokenStorage.getAccessToken() ?: ""
    }

    override fun refreshToken(): String? {
        val refreshToken = TokenStorage.getRefreshToken() ?: return null

        return try {
            val call = apiService.refreshTokenSync(RefreshTokenRequest(refreshToken))
            val result = call.execute()

            if (result.isSuccessful) result.body()?.data?.let { newToken ->
                val newAccessToken = newToken.accessToken
                val newRefreshToken = newToken.refreshToken

                Timber.tag(TAG).d("Token refresh success $newAccessToken ${newToken.accessTokenExpireAt}")

                updateToken(newAccessToken)
                updateRefreshToken(newRefreshToken)

                newAccessToken
            } else {
                Timber.tag(TAG).d("Token refresh failed")
                null
            }
        } catch (e: Exception) {
            Timber.tag(TAG).d(e, "Token refresh failed")
            null
        }
    }

    override fun updateToken(newToken: String) {
        TokenStorage.saveAccessToken(newToken)
    }

    private fun updateRefreshToken(newToken: String) {
        TokenStorage.saveRefreshToken(newToken)
    }

    override fun clearTokens() {
        TokenStorage.clearTokens()
    }
}
