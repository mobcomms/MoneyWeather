package com.moneyweather.event.main

import android.content.Context

sealed interface MainUiEvent {

    object RefreshFcmToken : MainUiEvent

    data class OpenOfferwall(val context: Context, val landingEventId: Int) : MainUiEvent

    object FetchPushAgree : MainUiEvent

    data class UpdatePushAgree(
        val servicePushAgreed: Boolean,
        val marketingPushAgreed: Boolean,
        val nightPushAllowed: Boolean
    ) : MainUiEvent

    object FetchPomissionZoneUrl : MainUiEvent
}