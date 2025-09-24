package com.moneyweather.event.sunflowersetting

sealed interface SettingSunflowerUiEvent {
    object FetchSettingInfo : SettingSunflowerUiEvent

    object CheckSettingSound : SettingSunflowerUiEvent

    object CheckSettingVibrate : SettingSunflowerUiEvent
}