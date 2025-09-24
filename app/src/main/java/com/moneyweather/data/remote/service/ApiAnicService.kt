package com.moneyweather.data.remote.service

import com.moneyweather.data.remote.response.AnicGameFailResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiAnicService {

    @Headers("Content-Type: application/json")
    @POST("/API/moneyweather/set_game_fail.php")
    fun anicGameFail(@Body body: HashMap<String, Any?>): Single<AnicGameFailResponse>
}