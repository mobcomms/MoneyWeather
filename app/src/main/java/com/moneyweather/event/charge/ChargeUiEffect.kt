package com.moneyweather.event.charge

sealed interface ChargeUiEffect {

    object ShowVerification : ChargeUiEffect

    object ShowBuzzvilOfferWall : ChargeUiEffect

    data class ShowToast(val message: String) : ChargeUiEffect
}