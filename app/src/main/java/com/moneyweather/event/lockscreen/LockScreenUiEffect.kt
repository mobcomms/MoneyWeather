package com.moneyweather.event.lockscreen

sealed interface LockScreenUiEffect {

    data class RefreshData(val fromCreated: Boolean, val isThemeChange: Boolean) : LockScreenUiEffect

}