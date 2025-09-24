package com.moneyweather.data.remote.impl

import com.moneyweather.data.remote.model.ApiAnicModel
import com.moneyweather.data.remote.response.AnicGameFailResponse
import com.moneyweather.data.remote.service.ApiAnicService
import io.reactivex.Single
import javax.inject.Inject

class ApiAnicImpl @Inject constructor(
    private val service: ApiAnicService
) : ApiAnicModel {

    override fun anicGameFail(m: HashMap<String, Any?>):
            Single<AnicGameFailResponse> = service.anicGameFail(m)
}