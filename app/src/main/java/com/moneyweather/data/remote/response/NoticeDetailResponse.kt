package com.moneyweather.data.remote.response

data class NoticeDetailResponse(
    var result: Int? = 0,
    var msg: String? = "",
    var data: Data
) {

    data class Data(
        var noticeId: Int? = 0,
        var title: String? = "",
        var content: String? = "",
        var isRead: Boolean? = false,
        var createdAt: String? = ""
    )
}