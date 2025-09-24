package com.moneyweather.event.theme

sealed interface ThemeWeatherUiEvent {

    object FetchHourlyWeather : ThemeWeatherUiEvent

    object FetchWeeklyWeather : ThemeWeatherUiEvent
}