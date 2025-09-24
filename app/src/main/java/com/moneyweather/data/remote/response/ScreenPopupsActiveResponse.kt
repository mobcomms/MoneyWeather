package com.moneyweather.data.remote.response

data class ScreenPopupsActiveResponse(var result: Int, var msg: String, var data: Data) {
    data class Data(
        var imageUrl: String? = "",
        var isExist: Boolean? = false,
        var linkedNoticeId: Int? = 0
    )
}