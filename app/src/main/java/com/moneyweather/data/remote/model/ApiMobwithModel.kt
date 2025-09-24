package com.moneyweather.data.remote.model

import io.reactivex.Single
import okhttp3.ResponseBody

interface ApiMobwithModel {

    fun getMobwithScriptBanner(m: HashMap<String, Any?>): Single<ResponseBody>
}