package com.moneyweather.event.sunflowersetting

sealed interface SettingSunflowerUiEffect {

    data class ErrorMessage(val message: String) : SettingSunflowerUiEffect

}