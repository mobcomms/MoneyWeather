package com.moneyweather.data.remote.model

import com.moneyweather.data.remote.response.AnicGameFailResponse
import io.reactivex.Single

interface ApiAnicModel {

    fun anicGameFail(m: HashMap<String, Any?>): Single<AnicGameFailResponse>
}