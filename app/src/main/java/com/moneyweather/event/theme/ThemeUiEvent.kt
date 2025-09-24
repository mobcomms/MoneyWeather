package com.moneyweather.event.theme

import com.moneyweather.event.lockscreen.NewsType
import com.moneyweather.model.enums.ThemeType

sealed interface ThemeUiEvent {

    object RefreshUserPoint : ThemeUiEvent

    object FetchRemoteConfigData : ThemeUiEvent

    data class FetchThemeData(val themeType: ThemeType) : ThemeUiEvent

    object CheckWeatherForecastData : ThemeUiEvent

    object FetchHolidays : ThemeUiEvent

    object UpdateThemeType : ThemeUiEvent

    object UpdateNews : ThemeUiEvent

    object CheckBottomBannerEarnPoint : ThemeUiEvent

    object SelectWarningNews : ThemeUiEvent

    data class SelectNoticeNews(val noticeId: Int) : ThemeUiEvent

    data class SelectNews(val articleUrl: String, val guid: String) : ThemeUiEvent

    data class CloseNews(val newsType: NewsType) : ThemeUiEvent

    object FetchWeeklyWeather : ThemeUiEvent

    data class SaveMobOnDongDongReward(
        val dailyIntervalConfigId: Int,
        val configId: Int,
        val index: Int,
        val point: Int
    ) : ThemeUiEvent

    data class SavePoint(val point: Int) : ThemeUiEvent
}