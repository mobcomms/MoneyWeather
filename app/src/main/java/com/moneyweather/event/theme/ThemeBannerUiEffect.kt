package com.moneyweather.event.theme

sealed interface ThemeBannerUiEffect {

    data class ShowMobWithScriptBanner(val zone: String, val script: String) : ThemeBannerUiEffect

    data class ShowBottomBannerPoint(val isAvailable: Boolean, val point: Int) : ThemeBannerUiEffect
}