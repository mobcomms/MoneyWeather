package com.moneyweather.event.my

sealed interface MyUiEvent {

    object Logout : MyUiEvent
}