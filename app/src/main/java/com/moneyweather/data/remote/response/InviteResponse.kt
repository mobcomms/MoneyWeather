package com.moneyweather.data.remote.response

data class InviteResponse(val data: Data) {

    data class Data(
        val inviteCode: String,
        val imageUrl: String,
        val description: String,
        val redirectUrl: String,
        val kakaoRedirectUrl: String,
        val isVerified: Boolean,
        val isRedeemed: Boolean
    )
}