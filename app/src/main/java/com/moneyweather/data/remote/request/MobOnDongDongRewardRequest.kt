package com.moneyweather.data.remote.request

import com.google.gson.annotations.SerializedName

data class MobOnDongDongRewardRequest(
    @SerializedName("mobonDongdongDailyIntervalConfigId")
    val mobonDongdongDailyIntervalConfigId: Int,
    @SerializedName("mobonDongdongConfigId")
    val mobonDongdongConfigId: Int,
    @SerializedName("isFirstParticipate")
    val isFirstParticipate: String
)