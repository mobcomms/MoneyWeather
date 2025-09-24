package com.moneyweather.data.remote.service

import com.moneyweather.data.remote.response.AccessTokenResponse
import com.moneyweather.data.remote.response.AutoMissionResponse
import com.moneyweather.data.remote.response.ParticipationCheckResponse
import com.moneyweather.data.remote.response.ParticipationResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface ApiPoMissionService {

    /**
     * 토큰 재발급
     *
     * @param version
     * @param map
     */
    @Headers("Content-Type: application/json")
    @GET("/api/v{version}/common/getAccessToken")
    fun getAccessToken(
        @Header("x-refresh-token") xRefreshToken: String,
        @Path("version") version: String,
        @QueryMap map: HashMap<String, Any?>
    ): Single<AccessTokenResponse>

    /**
     * 자동 미션 리스트 조회
     *
     * @param version
     * @param map
     */
    @Headers("Content-Type: application/json")
    @GET("/api/v{version}/mission/auto/list")
    fun autoMissionList(
        @Header("x-access-token") xAccessToken: String,
        @Path("version") version: String,
        @QueryMap map: HashMap<String, Any?>
    ): Single<AutoMissionResponse>

    /**
     * 참여 가능한 미션 여부 체크
     *
     * @param version
     * @param body
     */
    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/mission/participationCheck")
    fun participationCheck(
        @Header("x-access-token") xAccessToken: String,
        @Path("version") version: String,
        @Body body: HashMap<String, Any?>
    ): Single<ParticipationCheckResponse>

    /**
     * 미션 참여 완료
     *
     * @param version
     * @param body
     */
    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/mission/participation")
    fun participation(
        @Header("x-access-token") xAccessToken: String,
        @Path("version") version: String,
        @Body body: HashMap<String, Any?>
    ): Single<ParticipationResponse>
}