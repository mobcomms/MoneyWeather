package com.moneyweather.data.remote.response

import com.moneyweather.model.*

data class RegionResponse(var result: Int, var msg: String,var isCurrent: Boolean, var data: ArrayList<Data>) {
    data class Data(
        var region: Region,
        var weather : Weather,
        var isPermission: Boolean
    )
}