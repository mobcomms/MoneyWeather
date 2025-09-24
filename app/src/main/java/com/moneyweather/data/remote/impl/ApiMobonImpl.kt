package com.moneyweather.data.remote.impl

import com.moneyweather.data.remote.model.ApiMobonModel
import com.moneyweather.data.remote.response.AdNonSDKMobileBannerResponse
import com.moneyweather.data.remote.service.ApiMobonService
import io.reactivex.Single
import javax.inject.Inject

class ApiMobonImpl @Inject constructor(
    private val service: ApiMobonService
) : ApiMobonModel {

    override fun adNonSDKMobileBanner(m: HashMap<String, Any?>):
            Single<AdNonSDKMobileBannerResponse> = service.adNonSDKMobileBanner(m)
}