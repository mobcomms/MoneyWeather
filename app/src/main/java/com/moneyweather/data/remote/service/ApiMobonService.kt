package com.moneyweather.data.remote.service

import com.moneyweather.data.remote.response.AdNonSDKMobileBannerResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.QueryMap

interface ApiMobonService {

    /**
     * 모바일 배너 광고 정보
     *
     * @param map
     */
    @Headers("Content-Type: application/json")
    @GET("/servlet/adNonSDKMobileBanner")
    fun adNonSDKMobileBanner(@QueryMap map: HashMap<String, Any?>): Single<AdNonSDKMobileBannerResponse>
}