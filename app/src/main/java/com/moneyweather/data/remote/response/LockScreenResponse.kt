package com.moneyweather.data.remote.response

import android.os.Parcelable
import com.moneyweather.model.LockNotice
import com.moneyweather.model.News
import com.moneyweather.model.NewsRewardInfo
import com.moneyweather.model.Region
import com.moneyweather.model.Weather

data class LockScreenResponse(
    val result: Int,
    val msg: String,
    val data: Data
) {

    data class Data(
        val currentBalance: Int,
        val availableClickPoints: Int,
        val newsfeed: Newsfeed,
        val weatherInfo: WeatherInfo,
        val missions: ArrayList<Mission>,
        val campaignInfo: CampaignInfo,
        val lockLandingEventInfo: LockLandingEventInfo,
        val weatherForecastMessage: WeatherForecastMessage,
        val mobonDongDongInfo: MobonDongDongInfo,
        val lockEventSequence: List<String>,
        val coupangUrl: String,
        val pomissionZoneInfo: PomissionZoneInfo
    )

    data class Newsfeed(
        val newsRewardInfo: NewsRewardInfo,
        val news: ArrayList<News>,
        val warningTitle: String,
        val notice: LockNotice
    )

    data class WeatherInfo(
        val region: Region,
        val nowWeather: Weather,
        val hourWeathers: ArrayList<Weather>,
        val weekWeathers: ArrayList<Weather>
    )

    data class Mission(
        val isAvailable: Boolean,
        val startTime: String,
        val endTime: String
    )

    @kotlinx.parcelize.Parcelize
    data class CampaignInfo(
        val hasPriority: Boolean,
        val isExist: Boolean,
        val point: Int,
        val thresholdSec: Int
    ) : Parcelable

    data class LockLandingEventInfo(
        val lockLandingEventId: Int,
        val landingEventId: Int,
        val landingMediaId: Int,
        val navigator: String
    )

    data class WeatherForecastMessage(
        val forecastMessage: String
    )

    @kotlinx.parcelize.Parcelize
    data class MobonDongDongInfo(
        val isVisible: Boolean,
        val mobonDongdongDailyIntervalConfigId: Int,
        val mobonDongDongConfigInfoList: List<MobonDongDongConfigInfo>
    ) : Parcelable

    @kotlinx.parcelize.Parcelize
    data class MobonDongDongConfigInfo(
        val mobonDongdongConfigId: Int,
        val paymentTime: Int,
        val paymentPoint: Int
    ) : Parcelable

    @kotlinx.parcelize.Parcelize
    data class PomissionZoneInfo(
        val url: String,
        val isEnterPointAvailable: Boolean
    ) : Parcelable
}