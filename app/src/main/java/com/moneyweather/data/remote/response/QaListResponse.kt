package com.moneyweather.data.remote.response

import com.moneyweather.model.Qa

data class QaListResponse(var result: Int, var msg: String,var data: Data) {
    data class Data(
        var list: ArrayList<Qa>
    )
}