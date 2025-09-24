package com.moneyweather.data.remote.response

import android.text.TextUtils

data class QaDetailResponse(var result: Int, var msg: String, var data: Data) {
    data class Data(
        var inquiryId: Int,
        var title: String?,
        var content: String?,
        var reply: String?,
        var createdAt: String?,
    )

    fun isReplied(): Boolean {
        return !TextUtils.isEmpty(data.reply)
    }

}