package com.moneyweather.event.pushsetting

sealed interface PushSettingUiEvent {

    object FetchPushAgree : PushSettingUiEvent

    data class UpdatePushAgree(
        val servicePushAgreed: Boolean,
        val marketingPushAgreed: Boolean,
        val nightPushAllowed: Boolean
    ) : PushSettingUiEvent
}