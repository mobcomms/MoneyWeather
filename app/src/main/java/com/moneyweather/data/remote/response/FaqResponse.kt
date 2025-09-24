package com.moneyweather.data.remote.response

import com.moneyweather.model.Faq

data class FaqResponse(var result: Int, var msg: String,var data:Data) {
    data class Data(
        var list: ArrayList<Faq>
    )
}