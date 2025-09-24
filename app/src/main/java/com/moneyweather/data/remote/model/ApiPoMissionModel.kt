package com.moneyweather.data.remote.model

import com.moneyweather.data.remote.response.AccessTokenResponse
import com.moneyweather.data.remote.response.AutoMissionResponse
import com.moneyweather.data.remote.response.ParticipationCheckResponse
import com.moneyweather.data.remote.response.ParticipationResponse
import io.reactivex.Single

interface ApiPoMissionModel {

    fun getAccessToken(m: HashMap<String, Any?>): Single<AccessTokenResponse>
    fun autoMissionList(m: HashMap<String, Any?>): Single<AutoMissionResponse>
    fun participationCheck(m: HashMap<String, Any?>): Single<ParticipationCheckResponse>
    fun participation(m: HashMap<String, Any?>): Single<ParticipationResponse>
}