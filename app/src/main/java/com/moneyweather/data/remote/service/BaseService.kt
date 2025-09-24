package com.moneyweather.data.remote.service

import io.reactivex.Single
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface BaseService {
    @Headers("Content-Type: application/json")
    @POST("{path}/{value}")
    fun post(@Path(value = "path", encoded = true) path: String, @Path(value = "value", encoded = true) value: String, @Body params: JSONObject): Single<JSONObject>

    @Headers("Content-Type: application/json")
    @POST("{path}")
    fun post(@Path(value = "path", encoded = true) path: String, @Body params: JSONObject): Single<JSONObject>
}