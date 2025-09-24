package com.moneyweather.model

class ProductItem {
    var goodsId : String? = ""
    var goodsNo : String? = ""
    var category: String? = ""
    var affiliate: String? = ""
    var goodsName: String? = ""
    var goodsDescription: String? = ""
    var goodsImageSmall: String? = ""
    var salePrice: String? = ""
    var limitDay: Int? = 0
    var caution: String? = ""

    override fun toString(): String {
        return "ShopCategoryItem(goodsId=$goodsId,goodsNo=$goodsNo, affiliate=$affiliate, goodsName=$goodsName," +
                "goodsDescription=$goodsDescription,goodsImageSmall=$goodsImageSmall,salePrice=$salePrice,limitDay=$limitDay,caution=$caution)"
    }

}