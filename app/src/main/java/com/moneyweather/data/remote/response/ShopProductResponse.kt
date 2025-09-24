package com.moneyweather.data.remote.response

import com.moneyweather.model.*

data class ShopProductResponse(var result: Int, var count : Int, var goods: ArrayList<ShopProductItem>)