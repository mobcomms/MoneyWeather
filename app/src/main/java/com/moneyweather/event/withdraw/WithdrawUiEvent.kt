package com.moneyweather.event.withdraw

sealed interface WithdrawUiEvent {

    object RequestWithdraw : WithdrawUiEvent
}