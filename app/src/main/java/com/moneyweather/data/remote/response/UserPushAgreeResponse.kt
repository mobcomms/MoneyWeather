package com.moneyweather.data.remote.response

data class UserPushAgreeResponse(
    val result: Int,
    val msg: String,
    val data: Data
) {
    data class Data(
        val servicePushAgreed: String?,
        val marketingPushAgreed: String?,
        val nightPushAllowed: String?
    )
}