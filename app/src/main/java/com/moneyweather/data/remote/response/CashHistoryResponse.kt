package com.moneyweather.data.remote.response

import com.moneyweather.model.CashHistoryItem

data class CashHistoryResponse(var result: Int, var msg: String, val withdrawal: Int,var store : Int,var list: ArrayList<CashHistoryItem>)