package com.moneyweather.event.theme

import com.moneyweather.data.remote.response.AdNonSDKMobileBannerResponse
import com.moneyweather.data.remote.response.AutoMissionResponse
import com.moneyweather.data.remote.response.LockScreenResponse.LockLandingEventInfo
import com.moneyweather.model.Weather
import com.moneyweather.model.enums.ThemeType

sealed interface ThemeUiEffect {

    data class RefreshBackgroundTheme(val themeType: ThemeType, val weather: Weather) : ThemeUiEffect

    data class ShowEarnPointMessage(val point: Int, val isBottomBanner: Boolean) : ThemeUiEffect

    object ShowWarningNews : ThemeUiEffect

    data class ShowNoticeNews(val noticeId: Int) : ThemeUiEffect

    data class ShowNews(val articleUrl: String, val guid: String) : ThemeUiEffect

    data class ShowCoupangCpsButton(val url: String) : ThemeUiEffect

    data class ShowLiveCampaignButton(val liveCampaign: AdNonSDKMobileBannerResponse.Client) : ThemeUiEffect

    data class ShowAutoMissionButton(val autoMission: AutoMissionResponse?) : ThemeUiEffect

    data class ShowDongDongButton(val dongDong: AdNonSDKMobileBannerResponse.Data) : ThemeUiEffect

    object ShowPointButton : ThemeUiEffect

    data class ShowLandingEventButton(val landingEvent: LockLandingEventInfo) : ThemeUiEffect

    object ShowNoPointAction : ThemeUiEffect

    object ShowEarnPointAction : ThemeUiEffect
}