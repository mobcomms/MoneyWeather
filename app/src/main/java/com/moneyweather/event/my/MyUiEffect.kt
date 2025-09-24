package com.moneyweather.event.my

sealed interface MyUiEffect {

    data class MyPageMenuSetting(val loginState: Boolean) : MyUiEffect

    object ShowLogoutPopup : MyUiEffect
}