package com.moneyweather.data.remote.response

import com.moneyweather.model.*

data class ShopProductListResponse(var result: Int,var data : Data){
    data class Data(
        var pageInfo : PageInfo,
        var list: ArrayList<ShopProductItem>
    )

    data class PageInfo(
        var totalCount : Int,
        var page : Int,
        var showCount : Int
    )

}