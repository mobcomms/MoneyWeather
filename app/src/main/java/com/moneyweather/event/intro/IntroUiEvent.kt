package com.moneyweather.event.intro

import com.moneyweather.model.enums.ActivityEnum

sealed interface IntroUiEvent {

    object FetchAppVersion : IntroUiEvent

    object RequestGuestLogin : IntroUiEvent

    data class RequestLogin(
        val moveActivity: ActivityEnum?,
        val path: String?
    ) : IntroUiEvent

    data class RefreshSession(
        val moveActivity: ActivityEnum?,
        val path: String?
    ) : IntroUiEvent

    object MoveToLogin : IntroUiEvent
}