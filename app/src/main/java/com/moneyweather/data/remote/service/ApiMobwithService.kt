package com.moneyweather.data.remote.service

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface ApiMobwithService {

    /**
     * 광고 Script 송출
     *
     * @param map
     */
    @GET("/api/banner/app/mobchallenger/v1/donsee")
    fun getMobwithScriptBanner(@QueryMap map: HashMap<String, Any?>): Single<ResponseBody>
}