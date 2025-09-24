package com.moneyweather.viewmodel

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.moneyweather.BuildConfig
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.constants.ThemeConstants
import com.moneyweather.data.remote.model.ApiMobonModel
import com.moneyweather.data.remote.model.ApiMobwithModel
import com.moneyweather.data.remote.model.ApiPoMissionModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.data.remote.response.AdNonSDKMobileBannerResponse
import com.moneyweather.data.remote.response.AutoMissionResponse
import com.moneyweather.data.remote.response.LockScreenResponse
import com.moneyweather.data.remote.response.LockScreenResponse.LockLandingEventInfo
import com.moneyweather.data.remote.response.LockScreenResponse.Mission
import com.moneyweather.data.remote.response.NoticeDetailResponse
import com.moneyweather.data.remote.response.UserPointResponse
import com.moneyweather.data.remote.response.convertToCalendarList
import com.moneyweather.event.EventDelegate
import com.moneyweather.event.EventReplayDelegate
import com.moneyweather.event.lockscreen.NewsType
import com.moneyweather.event.theme.ThemeBannerUiEffect
import com.moneyweather.event.theme.ThemeBannerUiEvent
import com.moneyweather.event.theme.ThemeUiEffect
import com.moneyweather.event.theme.ThemeUiEvent
import com.moneyweather.extensions.throttleFirst
import com.moneyweather.model.AppInfo
import com.moneyweather.model.NewsRewardInfo
import com.moneyweather.model.Weather
import com.moneyweather.model.enums.ActivityEnum
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.model.enums.ThemeType
import com.moneyweather.ui.lockscreen.LockScreenBannerDelegate.Companion.POINT_EARN_TIME
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.CustomToast
import com.moneyweather.util.DateUtil
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.Logger
import com.moneyweather.util.NetworkUtils
import com.moneyweather.util.PrefRepository
import com.moneyweather.util.analytics.GaAdNewsClickEvent
import com.moneyweather.util.analytics.GaIconClickEvent
import com.moneyweather.util.remoteConfig.RemoteConfigManager
import com.moneyweather.view.LockScreenActivity
import com.moneyweather.view.LockScreenActivity.ButtonType.COUPANG_CPS
import com.moneyweather.view.LockScreenActivity.ButtonType.DONG_DONG
import com.moneyweather.view.LockScreenActivity.ButtonType.LIVE_STREAMING
import com.moneyweather.view.LockScreenActivity.ButtonType.MISSION
import com.moneyweather.view.LockScreenActivity.ButtonType.OFFERWALL
import com.moneyweather.view.LockScreenActivity.ButtonType.POINT
import com.moneyweather.view.LockScreenActivity.Companion.EXTRA_IS_LOCK_SCREEN
import com.moneyweather.view.LockScreenActivity.Companion.tuneEventPointSave
import com.moneyweather.view.LockScreenWebViewActivity
import com.moneyweather.view.LockScreenWebViewActivity.Companion.KEY_LOAD_URL
import com.moneyweather.view.LockScreenWebViewActivity.Companion.KEY_VIEW_TYPE
import com.moneyweather.view.MainActivity
import com.moneyweather.view.MyPointActivity
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val remoteConfigManager: RemoteConfigManager,
    val apiUserModel: ApiUserModel,
    private val apiPoMissionModel: ApiPoMissionModel,
    private val apiMobonModel: ApiMobonModel,
    private val apiMobwithModel: ApiMobwithModel
) : BaseKotlinViewModel(),
    EventDelegate<ThemeUiEffect, ThemeUiEvent> by EventDelegate.EventDelegateImpl(),
    EventReplayDelegate<ThemeBannerUiEffect, ThemeBannerUiEvent> by EventReplayDelegate.EventReplayDelegateImpl() {

    var newsRewardInfo: MutableLiveData<NewsRewardInfo> = MutableLiveData()
    var darkTheme: MutableLiveData<Boolean> = MutableLiveData()
    var resultNoticeDetail: MutableLiveData<NoticeDetailResponse.Data> = MutableLiveData()
    var missions: MutableLiveData<ArrayList<Mission>> = MutableLiveData()
    var autoMissionList: MutableLiveData<AutoMissionResponse> = MutableLiveData()
    var participationCheck: MutableLiveData<Int> = MutableLiveData()

    val currentUserPoint = MutableStateFlow(PrefRepository.LockQuickInfo.currentUserPoint)
    val availablePoint = MutableStateFlow(PrefRepository.LockQuickInfo.todayAvailablePoint)
    private val sunflowerClickCount = MutableStateFlow(0)

    private val lastApiCallDate = MutableStateFlow("")
    private val lastLocationCallDate = MutableStateFlow("")

    private val _campaignInfo: MutableStateFlow<LockScreenResponse.CampaignInfo> = MutableStateFlow(
        LockScreenResponse.CampaignInfo(hasPriority = false, isExist = false, point = 0, thresholdSec = 0)
    )
    val campaignInfo = _campaignInfo.asStateFlow()

    private val _mobonDongDongInfo: MutableStateFlow<LockScreenResponse.MobonDongDongInfo?> = MutableStateFlow(null)
    val mobonDongDongInfo = _mobonDongDongInfo.asStateFlow()

    private val _pomissionZonePointBadgeShown: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val pomissionZonePointBadgeShown = _pomissionZonePointBadgeShown.asStateFlow()

    private val _infoThemeData: MutableStateFlow<LockScreenResponse?> = MutableStateFlow(PrefRepository.LockQuickInfo.infoTheme)
    val infoThemeData = _infoThemeData.asStateFlow()

    private val _simpleThemeData: MutableStateFlow<LockScreenResponse?> = MutableStateFlow(PrefRepository.LockQuickInfo.simpleTheme)
    val simpleThemeData = _simpleThemeData.asStateFlow()

    private val _weatherForecast: MutableStateFlow<String> = MutableStateFlow("")
    val weatherForecast = _weatherForecast.asStateFlow()

    private val _weatherForecastShown: MutableStateFlow<Boolean> = MutableStateFlow(PrefRepository.LockQuickInfo.showWeatherForecast)
    val weatherForecastShown = _weatherForecastShown.asStateFlow()

    private val _bottomBannerPoint: MutableStateFlow<Int> = MutableStateFlow(1)
    val bottomBannerPoint = _bottomBannerPoint.asStateFlow()

    private val _holidays: MutableStateFlow<List<CalendarDay>> = MutableStateFlow(emptyList())
    val holidays = _holidays.asStateFlow()

    private val _refreshNews: MutableSharedFlow<Unit> = MutableSharedFlow()
    val refreshNews = _refreshNews.asSharedFlow().throttleFirst(300.milliseconds)

    private val _newsIndex = MutableStateFlow(0)
    val newsIndex = _newsIndex.asStateFlow()

    private val _newsFeedData: MutableStateFlow<LockScreenResponse.Newsfeed?> = MutableStateFlow(PrefRepository.LockQuickInfo.newsFeedData)
    val newsfeedData = _newsFeedData.asStateFlow()

    private val _weatherInfos: MutableStateFlow<List<Weather>?> =
        MutableStateFlow(PrefRepository.LockQuickInfo.infoTheme?.data?.weatherInfo?.hourWeathers)
    val weatherInfos = _weatherInfos.asStateFlow()

    private val _weatherImage: MutableStateFlow<Drawable?> = MutableStateFlow(AppInfo.currentWeatherInfo?.value?.weatherImage())
    val weatherImage = _weatherImage.asStateFlow()

    private val _weatherBackgroundImage: MutableStateFlow<Drawable?> = MutableStateFlow(AppInfo.currentWeatherInfo?.value?.weatherBackground())
    val weatherBackgroundImage = _weatherBackgroundImage.asStateFlow()

    private val _humidity: MutableStateFlow<Int?> = MutableStateFlow(AppInfo.currentWeatherInfo?.value?.humidity)
    val humidity = _humidity.asStateFlow()

    private val _temperature: MutableStateFlow<Float?> = MutableStateFlow(AppInfo.currentWeatherInfo?.value?.temp)
    val temperature = _temperature.asStateFlow()

    private val _apparentTemp: MutableStateFlow<Float?> = MutableStateFlow(AppInfo.currentWeatherInfo?.value?.apparentTemp)
    val apparentTemp = _apparentTemp.asStateFlow()

    private val _pm10Description: MutableStateFlow<String?> = MutableStateFlow(AppInfo.currentWeatherInfo?.value?.pm10Description)
    val pm10Description = _pm10Description.asStateFlow()

    private val _pm25Description: MutableStateFlow<String?> = MutableStateFlow(AppInfo.currentWeatherInfo?.value?.pm25Description)
    val pm25Description = _pm25Description.asStateFlow()

    private val _skyDescription: MutableStateFlow<String?> = MutableStateFlow(AppInfo.currentWeatherInfo?.value?.skyDescription)
    val skyDescription = _skyDescription.asStateFlow()

    private val _lockSimpleWeather: MutableStateFlow<SpannableStringBuilder?> = MutableStateFlow(AppInfo.currentWeatherInfo?.value?.lockSimpleWeather())
    val lockSimpleWeather = _lockSimpleWeather.asStateFlow()

    private val _lockCondition: MutableStateFlow<String?> = MutableStateFlow(AppInfo.currentWeatherInfo?.value?.lockCondition())
    val lockCondition = _lockCondition.asStateFlow()

    private val _lockCalendarCondition: MutableStateFlow<String?> = MutableStateFlow(AppInfo.currentWeatherInfo?.value?.lockCalendarCondition())
    val lockCalendarCondition = _lockCalendarCondition.asStateFlow()

    private val _backgroundThemeCondition: MutableStateFlow<String?> = MutableStateFlow(AppInfo.currentWeatherInfo?.value?.backgroundThemeCondition())
    val backgroundThemeCondition = _backgroundThemeCondition.asStateFlow()

    private val _savePoint: MutableSharedFlow<Unit> = MutableSharedFlow()
    val savePoint = _savePoint.asSharedFlow()

    companion object {
        const val NEWS_REPEAT_DELAY = 3_600L
        const val POINT_SAVE_DELAY = 800L
        const val HTTP_401_MOBON_NO_DATA = 401
    }

    init {
        savePoint
            .onEach {
                incrementSunflowerClickCount()
                incrementPoint(1)
                decrementAvailablePoint(1)
            }
            .debounce(POINT_SAVE_DELAY)
            .onEach {
                val count = sunflowerClickCount.value

                pointSave(count)
                decrementSunflowerClickCount(count)

                tuneEventPointSave(count)
            }
            .launchIn(viewModelScope)
    }

    private fun triggerNoPointAction() {
        viewModelScope.launch {
            emitEffect(ThemeUiEffect.ShowNoPointAction)
        }
    }

    private fun triggerSavePoint() {
        viewModelScope.launch {
            _savePoint.emit(Unit)
        }
    }

    private fun triggerPointAnimation() {
        viewModelScope.launch {
            emitEffect(ThemeUiEffect.ShowEarnPointAction)
        }
    }

    private fun incrementSunflowerClickCount() {
        sunflowerClickCount.update { it + 1 }
    }

    private fun decrementSunflowerClickCount(point: Int = 1) {
        sunflowerClickCount.update { it - point }
    }

    private fun resetSunflowerClickCount() {
        sunflowerClickCount.update { 0 }
    }

    private fun incrementPoint(point: Int = 1) {
        currentUserPoint.update { it + point }
    }

    private fun updatePoint(point: Int) {
        currentUserPoint.update { point }
    }

    fun resetPoint() {
        currentUserPoint.update { 0 }
    }

    private fun decrementAvailablePoint(point: Int = 1) {
        availablePoint.update { it - point }
    }

    private fun updateAvailablePoint(point: Int) {
        availablePoint.update { point }
    }

    private fun checkLastLocationCallDateValidity(): Boolean {
        val lastCall = lastLocationCallDate.value
        val isEmpty = TextUtils.isEmpty(lastCall)

        return isEmpty || DateUtil.intervalBetweenDateText(lastCall) >= ThemeConstants.LOAD_LOCATION_INTERVAL_MIN
    }

    private fun updateLastLocationCallDate() {
        lastLocationCallDate.update { DateUtil.getTime() }
    }

    /**
     * LOAD_THEME_DATA_INTERVAL_MIN(0분) 이내에 api 재요청 시 무시
     * TODO : 이벤트 데이터와 락스크린 데이터 분리 후 1분으로 늘릴 예정
     */
    private fun checkLastApiCallDateValidity(): Boolean {
        val lastCall = lastApiCallDate.value
        val isEmpty = TextUtils.isEmpty(lastCall)

        return isEmpty || DateUtil.intervalBetweenDateText(lastCall) >= ThemeConstants.LOAD_THEME_DATA_INTERVAL_MIN
    }

    private fun updateLastApiCallDate() {
        lastApiCallDate.update { DateUtil.getTime() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val newsContentsData = combine(_newsFeedData, _weatherForecast, refreshNews) { newsfeed, weatherForecast, _ ->
        newsfeed to weatherForecast
    }.flatMapLatest { data ->
        val newsfeed = data.first
        val forecast = data.second

        if (newsfeed == null) {
            flowOf(null)
        } else {
            when {
                isAvailableForecast(forecast) -> {
                    flowOf(NewsType.FORECAST to forecast)
                }

                isAvailableWarning(newsfeed?.warningTitle ?: "") -> {
                    flowOf(NewsType.WARNING to newsfeed.warningTitle)
                }

                isAvailableNotice(newsfeed.notice?.noticeId ?: 0) -> {
                    flowOf(NewsType.NOTICE to newsfeed.notice)
                }

                else -> {
                    // 뉴스 리스트 순회
                    flow<Pair<NewsType, Any>> {
                        coroutineContext.ensureActive()

                        val newsList = newsfeed.news
                        while (true) {
                            if (newsList.isNotEmpty()) {
                                val currentIndex = newsIndex.value
                                _newsIndex.value = (currentIndex + 1) % newsList.size

                                emit(NewsType.NEWS to newsList[currentIndex % newsList.size])
                            }

                            delay(NEWS_REPEAT_DELAY)
                        }
                    }
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private fun isAvailableForecast(forecastTitle: String): Boolean {
        return forecastTitle.isNotEmpty() && weatherForecastShown.value
    }

    private fun isAvailableWarning(warningTitle: String): Boolean {
        return warningTitle.isNotEmpty() && PrefRepository.LockQuickInfo.warningTitle != warningTitle
    }

    private fun isAvailableNotice(noticeId: Int): Boolean {
        return noticeId != 0 && PrefRepository.LockQuickInfo.noticeId != noticeId
    }

    override fun onCreate() {
        super.onCreate()
        incrementPoint(userPoint?.value?.currentPoint ?: 0)
        updateAvailablePoint(userPoint?.value?.todayAvailablePoints ?: 0)
        darkTheme.value = false
    }

    /**
     * Point 가 0 일 경우
     * - message 2초간 표시
     * Point 가 0 보다 많을 경우
     * - coin Animation 노출
     * - 300X250 Banner Click 로직 호출
     * - Point save api 호출
     */
    private fun handleSaveDefaultPoint() {
        if (availablePoint.value == 0) {
            triggerNoPointAction()
        } else {
            triggerPointAnimation()
            triggerSavePoint()
        }
    }

    override fun dispatchReplayEvent(event: ThemeBannerUiEvent) {
        when (event) {
            is ThemeBannerUiEvent.FetchBottomBannerPointAvailable -> {
                getBottomBannerPoint()
            }

            is ThemeBannerUiEvent.FetchMobWithScriptBanner -> {
                getMobwithScriptBanner(
                    zone = event.zoneId,
                    width = event.width,
                    height = event.height
                )
            }
        }
    }

    override fun dispatchEvent(event: ThemeUiEvent) {
        when (event) {
            is ThemeUiEvent.SavePoint -> {
                handleSaveDefaultPoint()
            }

            is ThemeUiEvent.RefreshUserPoint -> {
                connectUserPoint(true)
            }

            is ThemeUiEvent.FetchThemeData -> {
                getThemeData(themeType = event.themeType)
            }

            is ThemeUiEvent.FetchRemoteConfigData -> {
                observeRemoteConfig()
            }

            is ThemeUiEvent.CheckWeatherForecastData -> {
                checkWeatherForecastData()
            }

            is ThemeUiEvent.FetchHolidays -> {
                getHolidays()
            }

            is ThemeUiEvent.UpdateThemeType -> {
                simpleThemeData.value?.let {
                    val themeType = PrefRepository.SettingInfo.selectThemeType
                    val weather = it.data.weatherInfo.nowWeather

                    refreshBackgroundTheme(ThemeType.fromInt(themeType), weather)
                }
            }

            is ThemeUiEvent.CheckBottomBannerEarnPoint -> {
                checkBottomBannerEarnPoint()
            }

            is ThemeUiEvent.CloseNews -> {
                newsfeedData.value?.let {
                    closeAndUpdateNews(event.newsType, it)
                }
            }

            is ThemeUiEvent.UpdateNews -> {
                updateNews()
            }

            is ThemeUiEvent.SelectWarningNews -> {
                selectWarningNews()
            }

            is ThemeUiEvent.SelectNoticeNews -> {
                selectNoticeNews(event.noticeId)
            }

            is ThemeUiEvent.SelectNews -> {
                selectNews(event.articleUrl, event.guid)

                // Ga Log Event
                GaAdNewsClickEvent.logAdNewsClickEvent()
            }

            is ThemeUiEvent.FetchWeeklyWeather -> {
                getWeeklyWeather(
                    PrefRepository.LocationInfo.latitude,
                    PrefRepository.LocationInfo.longitude
                )
            }

            else -> {}
        }
    }

    private fun selectWarningNews() {
        viewModelScope.launch {
            emitEffect(ThemeUiEffect.ShowWarningNews)
        }
    }

    private fun selectNoticeNews(noticeId: Int) {
        viewModelScope.launch {
            emitEffect(ThemeUiEffect.ShowNoticeNews(noticeId))
        }
    }

    private fun selectNews(articleUrl: String, guid: String) {
        viewModelScope.launch {
            emitEffect(ThemeUiEffect.ShowNews(articleUrl, guid))
        }
    }

    private fun refreshBackgroundTheme(themeType: ThemeType, weather: Weather) {
        viewModelScope.launch {
            emitEffect(
                ThemeUiEffect.RefreshBackgroundTheme(
                    themeType = themeType,
                    weather = weather
                )
            )
        }
    }

    private fun checkWeatherForecastData() {
        val showWeatherForecast = !isSameDayAndAmPm(PrefRepository.UserInfo.lastSavedTimeMillis)
        PrefRepository.UserInfo.lastSavedTimeMillis = Date().time

        _weatherForecastShown.value = showWeatherForecast
        PrefRepository.LockQuickInfo.showWeatherForecast = showWeatherForecast
    }

    private fun isSameDayAndAmPm(savedTimeMillis: Long): Boolean {
        val savedCal = Calendar.getInstance().apply {
            timeInMillis = savedTimeMillis
        }

        val nowCal = Calendar.getInstance()

        val sameYear = savedCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
        val sameMonth = savedCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH)
        val sameDay = savedCal.get(Calendar.DAY_OF_MONTH) == nowCal.get(Calendar.DAY_OF_MONTH)
        val sameAmPm = savedCal.get(Calendar.AM_PM) == nowCal.get(Calendar.AM_PM)

        return sameYear && sameMonth && sameDay && sameAmPm
    }

    private fun observeRemoteConfig() {
        viewModelScope.launch {
            remoteConfigManager.configFlow.collect()
        }
    }

    private fun getThemeData(themeType: ThemeType) {
        if (!checkLastApiCallDateValidity()) return
        updateLastApiCallDate()

        // LOAD_LOCATION_INTERVAL_MIN(10분) 이내에 위치 재요청 시 기존 위치 데이터 사용
        if (!checkLastLocationCallDateValidity()) {
            fetchThemeData(
                themeType = themeType,
                lat = PrefRepository.LocationInfo.latitude,
                lon = PrefRepository.LocationInfo.longitude
            )
        } else {
            CommonUtils.getLocation(
                onSuccessCallbackListener = object : CommonUtils.Companion.OnCallbackLocationListener {
                    override fun onCallbackLocation(lat: String, lon: String) {
                        updateLastLocationCallDate()

                        fetchThemeData(
                            themeType = themeType,
                            lat = lat,
                            lon = lon
                        )
                    }
                },
                onFailureCallbackListener = object : CommonUtils.Companion.OnCallbackLocationListener {
                    override fun onCallbackLocation(lat: String, lon: String) {
                        fetchThemeData(
                            themeType = themeType,
                            lat = PrefRepository.LocationInfo.latitude,
                            lon = PrefRepository.LocationInfo.longitude
                        )
                    }
                },
            )
        }
    }

    private fun fetchThemeData(themeType: ThemeType, lat: String, lon: String) {
        if (themeType == ThemeType.INFO) {
            fetchInfoThemeData(lat, lon)
        } else {
            fetchSimpleThemeData(lat, lon)
        }
    }

    private fun updateNowWeather(weather: Weather) {
        viewModelScope.launch {
            _weatherImage.emit(weather.weatherImage())
            _weatherBackgroundImage.emit(weather.weatherBackground())
            _humidity.emit(weather.humidity)
            _temperature.emit(weather.temp)
            _apparentTemp.emit(weather.apparentTemp)
            _pm10Description.emit(weather.pm10Description)
            _pm25Description.emit(weather.pm25Description)
            _lockCondition.emit(weather.lockCondition())
            _lockSimpleWeather.emit(weather.lockSimpleWeather())
            _lockCalendarCondition.emit(weather.lockCalendarCondition())
            _skyDescription.emit(weather.skyDescription)
            _backgroundThemeCondition.emit(weather.backgroundThemeCondition())
        }
    }

    private fun fetchInfoThemeData(lat: String, lon: String) {
        var m = HashMap<String, Any?>()
        m["latitude"] = lat
        m["longitude"] = lon
        m["hourWeatherSize"] = 7
        m["newsSize"] = 5

        addDisposable(
            apiUserModel.infoLockScreen(m)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    PrefRepository.LockQuickInfo.infoTheme = it

                    _infoThemeData.value = it
                    _newsFeedData.value = it.data.newsfeed
                    _weatherInfos.value = it.data.weatherInfo.hourWeathers

                    updateNowWeather(it.data.weatherInfo.nowWeather)
                    setLockScreenData(it.data)

                    checkNextButton(buttonType = COUPANG_CPS)
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    fetchInfoThemeData(lat, lon)
                                }
                            }

                            else -> {
                                Logger.e(it.errorMessage)
                            }
                        }
                    }
                })
        )
    }

    private fun fetchSimpleThemeData(lat: String, lon: String) {
        var m = HashMap<String, Any?>()
        m["latitude"] = lat
        m["longitude"] = lon
        m["newsSize"] = 5

        addDisposable(
            apiUserModel.simpleLockScreen(m)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    PrefRepository.LockQuickInfo.simpleTheme = it

                    _simpleThemeData.value = it
                    _newsFeedData.value = it.data.newsfeed

                    updateNowWeather(it.data.weatherInfo.nowWeather)
                    setLockScreenData(it.data)

                    checkNextButton(buttonType = COUPANG_CPS)
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    fetchSimpleThemeData(lat, lon)
                                }
                            }

                            else -> {
                                Logger.e(it.errorMessage)
                            }
                        }
                    }
                })
        )
    }

    private fun closeAndUpdateNews(newsType: NewsType, newsfeedData: LockScreenResponse.Newsfeed) {
        if (newsType == NewsType.FORECAST) {
            checkWeatherForecastData()
        } else if (newsType == NewsType.WARNING) {
            PrefRepository.LockQuickInfo.warningTitle = newsfeedData.warningTitle
        } else if (newsType == NewsType.NOTICE) {
            PrefRepository.LockQuickInfo.noticeId = newsfeedData.notice?.noticeId ?: 0
        } else {
            // Noting to do..
        }

        updateNews()
    }

    private fun updateNews() {
        viewModelScope.launch {
            _refreshNews.emit(Unit)
        }
    }

    private fun setLockScreenData(data: LockScreenResponse.Data) {
        PrefRepository.LockQuickInfo.pomissionZoneUrl = data.pomissionZoneInfo.url
        PrefRepository.LockQuickInfo.pomissionZoneEnterPointAvailable = data.pomissionZoneInfo.isEnterPointAvailable
        PrefRepository.LockQuickInfo.newsFeedData = data.newsfeed
        PrefRepository.LockQuickInfo.lockLandingEventInfo = data.lockLandingEventInfo

        val pointData = UserPointResponse.Data(
            data.currentBalance ?: 0,
            data.availableClickPoints ?: 0,
            userPoint?.value?.todayEarnedClickPoints ?: 0
        )
        setPoint(pointData)

        setWhether(data.weatherInfo)
        setCampaign(data.campaignInfo)
        setMissions(data.missions)
        updateWeatherForecast(data.weatherForecastMessage)
        setMobOnAdEvent(data.mobonDongDongInfo)
        setPomissionZonePointBadge(data.pomissionZoneInfo.isEnterPointAvailable)
    }

    private fun setPoint(data: UserPointResponse.Data) {
        AppInfo.setUserPoint(data)
        updatePoint(data.currentPoint)
        updateAvailablePoint(data.todayAvailablePoints)
    }

    private fun setWhether(weatherInfo: LockScreenResponse.WeatherInfo) {
        AppInfo.setCurrentWeather(weatherInfo.nowWeather)
        AppInfo.setRegion(weatherInfo.region)
    }

    private fun updateWeatherForecast(weatherForecastMessage: LockScreenResponse.WeatherForecastMessage) {
        viewModelScope.launch {
            _weatherForecast.emit(weatherForecastMessage.forecastMessage)
        }
    }

    private fun setMissions(missionList: ArrayList<Mission>) {
        missions.value = missionList
    }

    private fun setCampaign(campaignInfo: LockScreenResponse.CampaignInfo) {
        viewModelScope.launch {
            _campaignInfo.emit(campaignInfo)
        }
    }

    private fun setMobOnAdEvent(mobonDongDongInfo: LockScreenResponse.MobonDongDongInfo) {
        viewModelScope.launch {
            _mobonDongDongInfo.emit(mobonDongDongInfo)
        }
    }

    private fun setPomissionZonePointBadge(isEnterPointAvailable: Boolean) {
        viewModelScope.launch {
            _pomissionZonePointBadgeShown.emit(isEnterPointAvailable)
        }
    }

    fun pointSave(point: Int) {
        addDisposable(
            apiUserModel.pointSave(point)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {
                        connectUserPoint()
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    pointSave(point)
                                }
                            }

                            else -> {
                                Logger.e(it.errorMessage)
                                connectUserPoint()
                            }

                        }
                    }
                })
        )
    }

    private fun connectUserPoint(fromRefresh: Boolean = false) {
        addDisposable(
            apiUserModel.userPoint()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    response.data?.let { data ->
                        setPoint(data)

                        if (!fromRefresh && data.todayAvailablePoints == 0) {
                            checkPointButton()
                        }
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectUserPoint()
                                }
                            }

                            else -> {
                                Logger.e(it.errorMessage)
                            }
                        }
                    }
                })
        )
    }

    fun connectNoticeDetail(noticeId: Int) {
        addDisposable(
            apiUserModel.noticeDetail(noticeId)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.run {
                        resultNoticeDetail.value = it.data
                    }
                }, {
                    val body = throwableCheck(it)
                    Logger.e(body.errorMessage)
                })
        )
    }

    private fun getHolidays() {
        addDisposable(
            apiUserModel.getHolidays()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _holidays.value = it.convertToCalendarList()
                }, {
                    val body = throwableCheck(it)
                    Logger.e(body.errorMessage)
                })
        )
    }

    private fun getPoMissionAccessToken() {
        val m = HashMap<String, Any?>()
        m["media_id"] = BuildConfig.POMISSION_MEDIA_ID

        addDisposable(
            apiPoMissionModel.getAccessToken(m)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    response.run {
                        if (token.isNotEmpty()) {
                            PrefRepository.UserInfo.xAccessToken = token
                            getAutoMissionList()
                        } else {
                            checkNextButton(buttonType = DONG_DONG)
                        }
                    }
                }, { throwable ->
                    checkNextButton(buttonType = DONG_DONG)
                    throwableCheck(throwable)
                })
        )
    }

    private fun getAutoMissionList() {
        val m = HashMap<String, Any?>()
        m["media_user_ad_id"] = PrefRepository.UserInfo.adid
        m["media_user_key"] = PrefRepository.UserInfo.userId
        m["last_mission_seq"] = ""
        m["mission_class"] = ""
        m["mission_page_count"] = "20"
        m["mission_type"] = ""
        m["os_type"] = "A"
        m["top_yn"] = ""

        addDisposable(
            apiPoMissionModel.autoMissionList(m)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (!response.auto.isNullOrEmpty() && !response.mission.isNullOrEmpty()) {
                        autoMissionList.value = response

                        showAutoMissionButton(response)
                    } else {
                        checkNextButton(buttonType = DONG_DONG)
                    }
                }, { throwable ->
                    checkNextButton(buttonType = DONG_DONG)
                    throwableCheck(throwable)
                })
        )
    }

    fun participationCheck(missionData: AutoMissionResponse, missionIdx: Int) {
        if (missionData?.mission == null) {
            return
        }

        var m = HashMap<String, Any?>()
        var missionSeq: Int? = 0
        var missionId: String? = ""

        if (missionData.mission.isNotEmpty() && missionData.mission.size > missionIdx) {
            missionSeq = missionData.mission[missionIdx].mission_seq
            missionId = missionData.mission[missionIdx].mission_id
        }
        m["mission_seq"] = missionSeq
        m["mission_id"] = missionId
        m["media_user_key"] = PrefRepository.UserInfo.userId
        m["media_user_ad_id"] = PrefRepository.UserInfo.adid
        m["media_app"] = ""
        m["media_user_phone"] = ""
        m["media_user_email"] = ""
        m["client_ip"] = ""
        m["device_name"] = ""
        m["carrier"] = ""
        m["custom"] = ""
        m["os"] = "a"
        m["sex"] = ""
        m["server_type"] = ""

        addDisposable(
            apiPoMissionModel.participationCheck(m)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.run {
                        participationCheck.value = it.result
                    }
                }, {
                    Logger.e(it.message)
                })
        )
    }

    /**
     * 모비온 모바일 배너 광고 정보 요청
     * @param code
     */
    private fun requestAdNonSDKMobileBanner(code: String) {
        val m = HashMap<String, Any?>()
        m["s"] = code // 매체 지면 번호
        m["bntype"] = BuildConfig.MOBON_BNTYPE // 타입값
        m["adid"] = PrefRepository.UserInfo.adid
        m["cntsr"] = 1 // 받고 싶은 상품 광고 갯수
        m["cntad"] = 1 // 받고 싶은 비상품 광고 갯수
        m["sdkYn"] = false
        m["increaseViewCnt"] = true // (옵션) 광고 호출시 노출수 count up 여부, 노출 API 사용시 필수 false

        addDisposable(
            apiMobonModel.adNonSDKMobileBanner(m)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    response.client?.let { client ->
                        if (HTTP_401_MOBON_NO_DATA == client[0].result_code) {
                            checkNextButtonForMobOn(code)
                            return@subscribe
                        }

                        when (code) {
                            BuildConfig.MOBON_CODE -> {
                                showLiveCampaignButton(client[0])
                            }

                            BuildConfig.MOBON_CODE_50_50 -> {
                                client[0].data?.let { adEvent ->
                                    showDongDongButton(adEvent[0])
                                }
                            }

                            else -> {}
                        }
                    } ?: run { checkNextButtonForMobOn(code) }

                }, { throwable ->
                    checkNextButtonForMobOn(code)
                    throwableCheck(throwable)
                })
        )
    }

    /**
     * 모비위드 스크립트 배너
     * @param zone 매체 지면 번호(고유값)
     * @param width 지면 가로 길이
     * @param height 지면 세로 길이
     */
    private fun getMobwithScriptBanner(zone: String, width: Int, height: Int) {
        var m = HashMap<String, Any?>()
        m["zone"] = zone
        m["count"] = 1 // 노출 광고 개수(현재 최대치는 1임)
        m["w"] = width
        m["h"] = height
        m["adid"] = PrefRepository.UserInfo.adid

        addDisposable(
            apiMobwithModel.getMobwithScriptBanner(m)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    Timber.tag(TAG).d("getMobwithScriptBanner success")
                    val script = response.string()

                    updateMobWithBannerScript(
                        zone = zone,
                        script = script
                    )
                }, {
                    Timber.tag(TAG).d("getMobwithScriptBanner failed")
                    throwableCheck(it)
                })
        )
    }

    private fun updateMobWithBannerScript(zone: String, script: String) {
        viewModelScope.launch {
            emitReplayEffect(ThemeBannerUiEffect.ShowMobWithScriptBanner(zone, script))
        }
    }

    private fun getLockscreenLandingEvent() {
        addDisposable(
            apiUserModel.lockscreenLandingEvent()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    response.data.run {
                        if (lockLandingEventInfo.lockLandingEventId > 0) {
                            showLandingEventButton(lockLandingEventInfo)
                        } else {
                            showPointButton()
                        }
                    }
                }, { throwable ->
                    showPointButton()
                    throwableCheck(throwable)
                })
        )
    }

    private fun getBottomBannerPoint() {
        addDisposable(
            apiUserModel.getLockScreenBottomBannerPoint()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    response.data.lockScreenBannerPointInfo.run {
                        _bottomBannerPoint.value = point
                        updateBottomBannerPoint(isPointAvailable, point)
                    }
                }, {
                    throwableCheck(it)
                })
        )
    }

    private fun updateBottomBannerPoint(isAvailable: Boolean, point: Int) {
        viewModelScope.launch {
            emitReplayEffect(ThemeBannerUiEffect.ShowBottomBannerPoint(isAvailable, point))
        }
    }

    private fun saveBottomBannerPoint() {
        val m = HashMap<String, Int>()
        m["point"] = bottomBannerPoint.value

        addDisposable(
            apiUserModel.saveLockScreenBottomBannerPoint(m)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showEarnPointMessage(
                        point = bottomBannerPoint.value,
                        isBottomBanner = true
                    )
                }, {
                    throwableCheck(it)
                })
        )
    }

    private fun saveBottomCoupangCpsPoint() {
        addDisposable(
            apiUserModel.saveLockScreenCoupangCpsPoint()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showEarnPointMessage(point = 1)
                }, {
                    throwableCheck(it)
                })
        )
    }

    private fun savePomissionZonePoint() {
        addDisposable(
            apiUserModel.saveLockScreenPomissionZonePoint()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    PrefRepository.LockQuickInfo.pomissionZoneEnterPointAvailable = false
                    showEarnPointMessage(point = 1)
                }, {
                    throwableCheck(it)
                })
        )
    }

    private fun getWeeklyWeather(lat: String, lon: String) {
        addDisposable(
            apiUserModel.weeklyWeather(lat, lon)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _weatherInfos.value = it.data.weathers
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    getWeeklyWeather(lat, lon)
                                }
                            }

                            else -> {
                                Logger.e(it.errorMessage)
                            }
                        }
                    }
                })
        )
    }

    private fun checkBottomBannerEarnPoint() {
        val clickedTime = PrefRepository.LockQuickInfo.bottomBannerClickedTime
        if (clickedTime == 0L) {
            return
        }

        val currentTime = System.currentTimeMillis()
        val stayTime = currentTime - clickedTime

        if (POINT_EARN_TIME > stayTime) {
            CustomToast.showToast(getContext(), getString(R.string.toast_stay_time_message))
        } else {
            saveBottomBannerPoint()
        }

        PrefRepository.LockQuickInfo.bottomBannerClickedTime = 0L
    }

    private fun showEarnPointMessage(point: Int, isBottomBanner: Boolean = false) {
        PrefRepository.LockQuickInfo.isShowEarnPointMessage = true

        viewModelScope.launch {
            emitEffect(ThemeUiEffect.ShowEarnPointMessage(point, isBottomBanner))
        }
    }

    private fun checkNextButtonForMobOn(code: String) {
        when (code) {
            BuildConfig.MOBON_CODE -> checkNextButton(buttonType = MISSION)
            BuildConfig.MOBON_CODE_50_50 -> checkPointButton()
        }
    }

    private fun getThemeData() = if (PrefRepository.SettingInfo.selectThemeType == ThemeType.INFO.type) _infoThemeData.value else _simpleThemeData.value

    private fun checkNextButton(buttonType: LockScreenActivity.ButtonType) {
        val themeData = getThemeData()?.data ?: return

        when (buttonType) {
            COUPANG_CPS -> checkCoupangCpsButton(themeData.coupangUrl)
            LIVE_STREAMING -> checkLiveCampaignButton(themeData)
            MISSION -> checkAutoMissionButton(themeData)
            DONG_DONG -> checkDongDongButton(themeData)
            OFFERWALL -> getLockscreenLandingEvent()
            POINT -> showPointButton()
        }
    }

    private fun checkCoupangCpsButton(url: String?) {
        if (url.isNullOrEmpty()) {
            checkNextButton(buttonType = MISSION)
        } else {
            showCoupangCpsButton(url)
        }
    }

    private fun checkLiveCampaignButton(data: LockScreenResponse.Data) {
        if (data.campaignInfo.hasPriority && data.campaignInfo.isExist) {
            requestAdNonSDKMobileBanner(BuildConfig.MOBON_CODE)
        } else {
            checkNextButton(buttonType = MISSION)
        }
    }

    private fun checkAutoMissionButton(data: LockScreenResponse.Data) {
        if (data.missions?.find { it.isAvailable } != null) {
            getPoMissionAccessToken()
        } else {
            checkNextButton(buttonType = DONG_DONG)
        }
    }

    private fun checkDongDongButton(data: LockScreenResponse.Data) {
        if (data.mobonDongDongInfo.isVisible) {
            requestAdNonSDKMobileBanner(BuildConfig.MOBON_CODE_50_50)
        } else {
            checkPointButton()
        }
    }

    private fun checkPointButton() {
        if (availablePoint.value > 0) {
            checkNextButton(buttonType = POINT)
        } else {
            checkNextButton(buttonType = OFFERWALL)
        }
    }

    private fun showCoupangCpsButton(url: String) {
        viewModelScope.launch {
            emitEffect(ThemeUiEffect.ShowCoupangCpsButton(url))
        }
    }

    private fun showLiveCampaignButton(liveCampaign: AdNonSDKMobileBannerResponse.Client) {
        viewModelScope.launch {
            emitEffect(ThemeUiEffect.ShowLiveCampaignButton(liveCampaign))
        }
    }

    private fun showAutoMissionButton(autoMission: AutoMissionResponse?) {
        viewModelScope.launch {
            emitEffect(ThemeUiEffect.ShowAutoMissionButton(autoMission))
        }
    }

    private fun showDongDongButton(dongDong: AdNonSDKMobileBannerResponse.Data) {
        viewModelScope.launch {
            emitEffect(ThemeUiEffect.ShowDongDongButton(dongDong))
        }
    }

    private fun showPointButton() {
        viewModelScope.launch {
            emitEffect(ThemeUiEffect.ShowPointButton)
        }
    }

    private fun showLandingEventButton(landingEvent: LockLandingEventInfo) {
        viewModelScope.launch {
            emitEffect(ThemeUiEffect.ShowLandingEventButton(landingEvent))
        }
    }

    fun onClickNaver() {
        if (NetworkUtils.checkNetworkState(getContext())) {
            val intent = Intent(getContext(), LockScreenWebViewActivity::class.java).apply {
                putExtra(KEY_LOAD_URL, CommonUtils.getWeatherSearchUrl(getContext()))
                putExtra(KEY_VIEW_TYPE, LockScreenWebViewActivity.ViewType.NORMAL.name)
                putExtra(FirebaseAnalyticsManager.VIEW_NAME, "네이버 날씨")
            }

            startActivity(intent)

            // Ga Log Event
            GaIconClickEvent.logLockScreenNaverWeatherIconClickEvent()
        }
    }

    fun onClickShopPlus() {
        val intent = Intent(getContext(), MainActivity::class.java).putExtra(
            "move_activity",
            ActivityEnum.SHOPLUS
        )
        startActivity(intent)

        // Ga Log Event
        GaIconClickEvent.logLockScreenShopRewardIconClickEvent()
    }

    fun onClickGame() {
        val intent = Intent(getContext(), MainActivity::class.java).putExtra(
            "move_activity",
            ActivityEnum.GAMEZONE
        )
        startActivity(intent)

        // Ga Log Event
        GaIconClickEvent.logLockScreenGameIconClickEvent()
    }

    fun onClickCharge() {
        val intent = Intent(getContext(), MainActivity::class.java).putExtra(
            "move_activity",
            ActivityEnum.CHARGE
        )
        startActivity(intent)

        // Ga Log Event
        GaIconClickEvent.logLockScreenOfferwallIconClickEvent()
    }

    fun onClickPomissionZone() {
        if (PrefRepository.LockQuickInfo.pomissionZoneUrl.isEmpty()) return

        val intent = Intent(getContext(), MainActivity::class.java).apply {
            putExtra("move_activity", ActivityEnum.CHARGE_POMISSION_ZONE)
            putExtra(EXTRA_IS_LOCK_SCREEN, false)
        }
        startActivity(intent)

        if (PrefRepository.LockQuickInfo.pomissionZoneEnterPointAvailable) {
            savePomissionZonePoint()
        }

        // Ga Log Event
        GaIconClickEvent.logLockScreenOfferwallPomissionIconClickEvent()
    }

    fun onClickOfferwall() {
        val intent = Intent(getContext(), MainActivity::class.java)
        intent.putExtra("move_activity", ActivityEnum.CHARGE)
        intent.putExtra("is_offerwall", true)
        startActivity(intent)
    }

    fun onClickCoupangCps() {
        val url = getThemeData()?.data?.coupangUrl?.takeIf { it.isNotEmpty() } ?: return

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)

        saveBottomCoupangCpsPoint()
    }

    fun onClickPoint() {
        if (NetworkUtils.checkNetworkState(getContext())) {
            val intent = Intent(getContext(), MyPointActivity::class.java)
            intent.putExtra("from_activity", "LockScreenActivity")
            intent.putExtra(EXTRA_IS_LOCK_SCREEN, true)
            startActivity(intent)
        }
    }

    fun minMaxTemp(index: Int): String {
        var result = ""

        weatherInfos.value?.let { weatherInfos ->
            result = String.format(
                getString(R.string.min_and_max_temp),
                weatherInfos[index].minTemp(),
                weatherInfos[index].maxTemp()
            )
        }

        return result
    }
}
