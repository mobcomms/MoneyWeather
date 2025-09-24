package com.moneyweather.data.remote.response

import com.moneyweather.model.*

data class NewsResponse(var result: Int, var msg: String, var data :Data){
    data class Data(
        var newsRewardInfo: NewsRewardInfo,
        var news: ArrayList<News>,
        var warningTitle: String,
        var notice : LockNotice,
    )
}