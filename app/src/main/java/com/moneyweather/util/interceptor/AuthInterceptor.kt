package com.moneyweather.util.interceptor

import com.moneyweather.model.enums.ResultCode
import com.moneyweather.util.token.TokenProvider
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider
) : Interceptor {

    private val refreshLock = Any()

    companion object {
        val TAG: String = AuthInterceptor::class.java.simpleName

        const val ERROR_CODE_NAME = "errorCode"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // response 의 code 가 Unauthorized(401) 이거나 error code 가 NO_VERIFIED(1104) 가 아닌 경우, 토큰을 갱신함.
        if (response.code == NetworkConstants.HTTP_401_UNAUTHORIZED && !checkVerificationError(response)) {
            response.close()

            val oldToken = tokenProvider.getToken()

            synchronized(refreshLock) {
                if (oldToken != tokenProvider.getToken()) return@synchronized

                val newToken = tokenProvider.refreshToken()
                if (newToken != null) {
                    Timber.tag(TAG).d("Token is refreshed : $newToken")
                    tokenProvider.updateToken(newToken)
                }
            }

            val newRequest = request.newBuilder()
                .removeHeader(NetworkConstants.AUTHORIZATION)
                .removeHeader(NetworkConstants.ACCESS_TOKEN)
                .addHeader(NetworkConstants.AUTHORIZATION, "${NetworkConstants.BEARER} ${tokenProvider.getToken()}")
                .addHeader(NetworkConstants.ACCESS_TOKEN, tokenProvider.getToken())
                .build()

            return chain.proceed(newRequest)
        }

        return response
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
}
