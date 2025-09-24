package com.moneyweather.event.mypoint

sealed interface MyPointUiEvent {

    object FetchAvailablePoints : MyPointUiEvent

    data class RefreshData(val type: Int, val page: Int) : MyPointUiEvent
}