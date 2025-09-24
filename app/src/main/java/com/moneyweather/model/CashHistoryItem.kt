package com.moneyweather.model

class CashHistoryItem {
    var cash : Float? = 0f
    var debit : Int? = 0
    var comment: String? = ""
    var type : Int? = 0
    var state : Int? = 0
    var goods_nm: String? = ""
    var reason: String? = ""
    var reg_date: String? = ""

    override fun toString(): String {
        return "ShopCategoryItem(cash=$cash,debit=$debit, comment=$comment, type=$type," +
                "state=$state,goods_nm=$goods_nm,reason=$reason,reg_date=$reg_date)"
    }

}