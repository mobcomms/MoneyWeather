package com.moneyweather.data.remote.response

import com.moneyweather.model.PointHistoryItem

data class PointHistoryResponse(var result: Int, var msg: String, var data:Data) {
    data class Data(
        var pageInfo : PageInfo,
        var list: ArrayList<PointHistoryItem>
    )

    data class PageInfo(
        var totalCount : Int,
        var page : Int,
        var showCount : Int
    )
}