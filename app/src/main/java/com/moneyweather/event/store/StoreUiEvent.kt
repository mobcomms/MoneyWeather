package com.moneyweather.event.store

sealed interface StoreUiEvent {

    object FetchAvailablePoints : StoreUiEvent
}