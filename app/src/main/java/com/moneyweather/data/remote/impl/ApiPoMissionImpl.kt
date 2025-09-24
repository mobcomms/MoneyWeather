package com.moneyweather.data.remote.impl

import com.moneyweather.BuildConfig
import com.moneyweather.data.remote.UrlHelper
import com.moneyweather.data.remote.model.ApiPoMissionModel
import com.moneyweather.data.remote.response.AccessTokenResponse
import com.moneyweather.data.remote.response.AutoMissionResponse
import com.moneyweather.data.remote.response.ParticipationCheckResponse
import com.moneyweather.data.remote.response.ParticipationResponse
import com.moneyweather.data.remote.service.ApiPoMissionService
import com.moneyweather.util.PrefRepository
import io.reactivex.Single
import javax.inject.Inject

class ApiPoMissionImpl @Inject constructor(
    private val service: ApiPoMissionService
) : ApiPoMissionModel {

    override fun getAccessToken(m: HashMap<String, Any?>): Single<AccessTokenResponse> {
        val xRefreshToken = BuildConfig.POMISSION_REFRESH_TOKEN
        val version = UrlHelper.POMISSION_API_VERSION
        return service.getAccessToken(xRefreshToken, version, m)
    }

    override fun autoMissionList(m: HashMap<String, Any?>): Single<AutoMissionResponse> {
        val xAccessToken = PrefRepository.UserInfo.xAccessToken
        val version = UrlHelper.POMISSION_API_VERSION
        return service.autoMissionList(xAccessToken, version, m)
    }

    override fun participationCheck(m: HashMap<String, Any?>): Single<ParticipationCheckResponse> {
        val xAccessToken = PrefRepository.UserInfo.xAccessToken
        val version = UrlHelper.POMISSION_API_VERSION
        return service.participationCheck(xAccessToken, version, m)
    }

    override fun participation(m: HashMap<String, Any?>): Single<ParticipationResponse> {
        val xAccessToken = PrefRepository.UserInfo.xAccessToken
        val version = UrlHelper.POMISSION_API_VERSION
        return service.participation(xAccessToken, version, m)
    }
}