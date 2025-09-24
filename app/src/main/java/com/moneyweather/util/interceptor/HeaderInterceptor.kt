package com.moneyweather.util.interceptor

import com.moneyweather.util.PrefRepository
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HeaderInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()
        val url = originalRequest.url

        getDefaultHeader().forEach { (key, value) ->
            builder.addHeader(key, value)
        }

        return chain.proceed(
            builder
                .url(url)
                .build(),
        )
    }

    private fun getDefaultHeader(): Map<String, String> = mapOf(
        NetworkConstants.ACCESS_TOKEN to PrefRepository.UserInfo.accessToken,
        NetworkConstants.AUTHORIZATION to "${NetworkConstants.BEARER} ${PrefRepository.UserInfo.accessToken}"
    )
}