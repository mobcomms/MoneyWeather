package com.moneyweather.model

import android.text.TextUtils

data class Region(
    val cityCode: String = "",
    val regionName: String = "",
    val depth1: String = "",
    val depth2: String = "",
    val depth3: String = "",
    val warnCode: String = ""
) {
    fun getLastDepth(): String = if (TextUtils.isEmpty(depth3)) depth2 else depth3

    override fun toString(): String {
        return "Region(cityCode=$cityCode, regionName=$regionName,depth1=$depth1,depth2=$depth2,depth3=$depth3,warnCode=$warnCode)"
    }
}