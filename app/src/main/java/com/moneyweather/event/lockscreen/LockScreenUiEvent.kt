package com.moneyweather.event.lockscreen

sealed interface LockScreenUiEvent {

    data class RefreshData(val fromCreated: Boolean, val isThemeChange: Boolean) : LockScreenUiEvent

    object AppUpdateCheck : LockScreenUiEvent
}