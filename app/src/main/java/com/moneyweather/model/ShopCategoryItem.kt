package com.moneyweather.model

class ShopCategoryItem {
    var category: String? = ""
    var affiliates: ArrayList<String>? = null

    override fun toString(): String {
        return "ShopCategoryItem(category=$category,affiliates=$affiliates)"
    }



    class AffiliateItem{
        var affiliate : String? = ""
    }
}