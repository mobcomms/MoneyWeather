package com.moneyweather.data.remote.impl

import com.moneyweather.data.remote.model.ApiMobwithModel
import com.moneyweather.data.remote.service.ApiMobwithService
import io.reactivex.Single
import okhttp3.ResponseBody
import javax.inject.Inject

class ApiMobwithImpl @Inject constructor(
    private val service: ApiMobwithService
) : ApiMobwithModel {

    override fun getMobwithScriptBanner(m: HashMap<String, Any?>): Single<ResponseBody> =
        service.getMobwithScriptBanner(m)
}