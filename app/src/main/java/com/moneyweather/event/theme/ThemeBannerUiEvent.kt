package com.moneyweather.event.theme

sealed interface ThemeBannerUiEvent {

    data class FetchMobWithScriptBanner(val zoneId: String, val width: Int, val height: Int) :
        ThemeBannerUiEvent

    object FetchBottomBannerPointAvailable : ThemeBannerUiEvent
}