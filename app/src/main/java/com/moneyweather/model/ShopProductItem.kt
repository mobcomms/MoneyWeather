package com.moneyweather.model

class ShopProductItem {
    var salePrice : Int? = 0
    var goodsName: String? = ""
    var limitDay : Int? = 0
    var goodsId: String? = ""
    var goodsNo : Int? = 0
    var goodsDescription: String? = ""
    var goodsImageSmall: String? = ""
    var brandName: String? = ""

    var end_date: String? = ""
    var category: String? = ""
    var affiliate: String? = ""

    override fun toString(): String {
        return "ShopCategoryItem(end_date=$end_date,total_price=$salePrice, goods_nm=$goodsName, category=$category," +
                "limitDay=$limitDay,goodsId=$goodsId,goodsNo=$goodsNo,goods_desc=$goodsDescription,affiliate=$affiliate," +
                "brandName=$brandName,goodsImageSmall=$goodsImageSmall )"
    }

}