package com.moneyweather.model

class CouponItem {
    var couponId: Int? = 0
    var expiredDate: String? = ""
    var salePrice: String? = ""
    var goodsId: String? = ""
    var trId: String? = ""
    var pinNo: String? = ""
    var affiliate: String? = ""
    var goodsName: String? = ""
    var goodsDescription: String? = ""
    var goodsImageSmall: String? = ""
    var limitDay: Int? = 0
    var daysUntilExpiry: Int = 0
    var status: Int? = 0

//    var msg : String? = ""
//    var total_price : Int? = 0
//    var state : Int? = 0
//    var note: String? = ""
//    var caution: String? = ""

    fun getLimit(): String {
        var ret = when(status) {
            0 -> {
                if (daysUntilExpiry == 0){
                    "오늘까지"
                } else if (daysUntilExpiry > 0) {
                    String.format("%d일 남음", daysUntilExpiry)
                } else {
                    "기간 만료"
                }
            }
            1 -> "사용"
            2 -> "환불"
            3 -> "기간 만료"
            else -> ""
        }

        return ret
    }
}