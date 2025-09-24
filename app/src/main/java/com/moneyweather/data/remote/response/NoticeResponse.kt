package com.moneyweather.data.remote.response

import com.moneyweather.model.Notice

data class NoticeResponse(var result: Int, var msg: String, var data:Data) {
    data class Data(
        var list: ArrayList<Notice>
    )
}