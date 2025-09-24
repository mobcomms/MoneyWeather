package com.moneyweather.util.interceptor

import com.moneyweather.model.enums.ResultCode
import com.moneyweather.util.token.TokenProvider
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenProvider: TokenProvider
) : Authenticator {

    private val refreshLock = Any()

    companion object {
        val TAG: String = TokenAuthenticator::class.java.simpleName

        const val ERROR_CODE_NAME = "errorCode"
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // 401이 여러 번 발생하면 무한 루프 방지
        if (responseCount(response) >= 2) return null

        val request = response.request

        if (response.code == NetworkConstants.HTTP_401_UNAUTHORIZED && !checkVerificationError(response)) {
            response.close()

            val oldToken = tokenProvider.getToken()
            var refreshSucceeded = false

            synchronized(refreshLock) {
                if (oldToken == tokenProvider.getToken()) {
                    val newToken = tokenProvider.refreshToken()
                    if (newToken != null) {
                        tokenProvider.updateToken(newToken)
                        Timber.tag(TAG).d("Token refreshed: $newToken")
                        refreshSucceeded = true
                    } else {
                        Timber.tag(TAG).e("Token refresh failed")
                        refreshSucceeded = false
                    }
                } else {
                    Timber.tag(TAG).d("Token already refreshed by another thread.")
                    refreshSucceeded = true
                }
            }

            return if (refreshSucceeded) {
                request.newBuilder()
                    .removeHeader(NetworkConstants.AUTHORIZATION)
                    .removeHeader(NetworkConstants.ACCESS_TOKEN)
                    .addHeader(NetworkConstants.AUTHORIZATION, "${NetworkConstants.BEARER} ${tokenProvider.getToken()}")
                    .addHeader(NetworkConstants.ACCESS_TOKEN, tokenProvider.getToken())
                    .build()
            } else {
                null
            }
        } else {
            return response.request
        }
    }

    /**
     * response 의 error code 가 NO_VERIFIED(1104) 인지 확인.
     *
     * @param response interceptor 에서 가로챈 request 의 response
     *
     * @return true :
     * - NO_VERIFIED(1104)
     * - false : NO_VERIFIED(1104) 가 아닌 경우
     */
    private fun checkVerificationError(response: Response): Boolean {
        val responseBody = response.peekBody(Long.MAX_VALUE)
        val bodyString = responseBody.string()

        return try {
            val json = JSONObject(bodyString)
            val errorCode = json.optInt(ERROR_CODE_NAME, -1)

            errorCode == ResultCode.NO_VERIFIED.resultCode

        } catch (e: Exception) {
            e.printStackTrace()

            true
        }
    }

    private fun responseCount(response: Response): Int {
        var res = response
        var count = 1
        while (res.priorResponse != null) {
            count++
            res = res.priorResponse!!
        }
        return count
    }
}