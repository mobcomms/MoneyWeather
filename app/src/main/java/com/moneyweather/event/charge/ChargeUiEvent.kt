package com.moneyweather.event.charge

sealed interface ChargeUiEvent {

    object CheckVerification : ChargeUiEvent
}