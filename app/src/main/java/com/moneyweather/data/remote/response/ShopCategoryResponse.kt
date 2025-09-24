package com.moneyweather.data.remote.response

import com.moneyweather.model.*

data class ShopCategoryResponse(var result: Int,var data : Data){
    data class Data(
        var categories: ArrayList<ShopCategoryItem>
    )
}