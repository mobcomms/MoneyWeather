package com.moneyweather.data.remote.model

import com.moneyweather.data.remote.response.AdNonSDKMobileBannerResponse
import io.reactivex.Single

interface ApiMobonModel {

    fun adNonSDKMobileBanner(m: HashMap<String, Any?>): Single<AdNonSDKMobileBannerResponse>
}