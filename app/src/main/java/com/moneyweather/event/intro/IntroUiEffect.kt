package com.moneyweather.event.intro

import com.moneyweather.data.remote.response.AppVersionResponse

sealed interface IntroUiEffect {

    data class CheckAppUpdate(val data: AppVersionResponse.Data?) : IntroUiEffect

    object CheckLogin : IntroUiEffect
}