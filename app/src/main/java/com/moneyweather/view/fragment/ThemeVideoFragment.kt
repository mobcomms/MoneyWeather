package com.moneyweather.view.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.constants.ThemeConstants
import com.moneyweather.data.remote.UrlHelper
import com.moneyweather.data.remote.response.AdNonSDKMobileBannerResponse
import com.moneyweather.data.remote.response.AutoMissionResponse
import com.moneyweather.data.remote.response.LockScreenResponse
import com.moneyweather.databinding.FragmentThemeVideoBinding
import com.moneyweather.event.lockscreen.LockScreenUiEffect
import com.moneyweather.event.lockscreen.NewsType
import com.moneyweather.event.theme.ThemeBannerUiEffect
import com.moneyweather.event.theme.ThemeUiEffect
import com.moneyweather.event.theme.ThemeUiEvent
import com.moneyweather.extensions.createExoPlayer
import com.moneyweather.extensions.hasUsageStatsPermission
import com.moneyweather.extensions.isScreenOn
import com.moneyweather.extensions.pauseVideo
import com.moneyweather.extensions.playVideo
import com.moneyweather.extensions.prepareVideo
import com.moneyweather.extensions.throttleFirst
import com.moneyweather.fcm.listener.setOnSingleClickListener
import com.moneyweather.model.LockNotice
import com.moneyweather.model.News
import com.moneyweather.model.Weather
import com.moneyweather.model.enums.ActivityEnum
import com.moneyweather.model.enums.DialogType
import com.moneyweather.model.enums.OfferwallEnum
import com.moneyweather.model.enums.ThemeType
import com.moneyweather.service.OverlayProgressService
import com.moneyweather.ui.dialog.HCCommonDialog
import com.moneyweather.ui.lockscreen.LockScreenBannerDelegate
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.CustomToast
import com.moneyweather.util.DateUtil
import com.moneyweather.util.EventBus.eventReceive
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.ForegroundServiceNotification.showNotification
import com.moneyweather.util.Logger
import com.moneyweather.util.NetworkUtils
import com.moneyweather.util.PrefRepository
import com.moneyweather.util.WeatherUtils
import com.moneyweather.util.analytics.GaAdButtonClickEvent
import com.moneyweather.util.analytics.GaButtonClickEvent
import com.moneyweather.util.analytics.GaIconClickEvent
import com.moneyweather.view.LockScreenActivity
import com.moneyweather.view.LockScreenActivity.Companion.EXTRA_IS_LOCK_SCREEN
import com.moneyweather.view.LockScreenActivity.Companion.FETCH_REFRESH_DATA_DELAY
import com.moneyweather.view.LockScreenActivity.Companion.animation
import com.moneyweather.view.LockScreenWebViewActivity
import com.moneyweather.view.LockScreenWebViewActivity.Companion.KEY_DONG_DONG
import com.moneyweather.view.LockScreenWebViewActivity.Companion.KEY_LOAD_URL
import com.moneyweather.view.LockScreenWebViewActivity.Companion.KEY_VIEW_TYPE
import com.moneyweather.view.SettingActivity
import com.moneyweather.viewmodel.LockScreenViewModel
import com.moneyweather.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import java.util.Timer
import java.util.TimerTask
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class ThemeVideoFragment : BaseKotlinFragment<FragmentThemeVideoBinding, ThemeViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_theme_video
    override val viewModel: ThemeViewModel by viewModels()
    private val activityViewModel: LockScreenViewModel by activityViewModels()

    private lateinit var bannerDelegate: LockScreenBannerDelegate

    private val scope = MainScope()

    private var watchedNews: String? = ""
    private var lastLoadBannerTimeOnCreate: String = ""

    private var landingEventInfo: LockScreenResponse.LockLandingEventInfo? = null
    private var campaignInfo: LockScreenResponse.CampaignInfo? = null
    private var campaignData: AdNonSDKMobileBannerResponse.Client? = null
    private var dongDongInfo: LockScreenResponse.MobonDongDongInfo? = null
    private var dongDongData: AdNonSDKMobileBannerResponse.Data? = null
    private var missionData: AutoMissionResponse? = null
    private var missionIdx: Int = 0

    private var skyCode: Int? = 0
    private var isSunrise: Boolean? = false

    private var exoPlayer: SimpleExoPlayer? = null

    companion object {
        const val PLAYER_SPEED = 0.5f
    }

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "락스크린 영상형 테마")
        })

        CommonUtils.setLockScreenSystemBarPaddingAndColor(requireActivity(), viewDataBinding.root, R.color.background_theme_status_bar_color, false)

        viewModel.darkTheme.value = true

        viewDataBinding.vm = viewModel
        viewDataBinding.topBar.vm = viewModel
        viewDataBinding.bottomArea.vm = viewModel

        // initialize UI
        initViews()
        initBottomLayoutViews()

        // observe ViewModel
        observeActivityViewModel()
        observeViewModel()

        setBannerDelegate()
        setEventReceive()
    }

    private fun initViews() {
        viewDataBinding.apply {
            rootLayout.onSwipeUnlockListener = {
                activity?.finish()
            }

            topBar.btnSetting.setOnClickListener {
                if (NetworkUtils.checkNetworkState(requireContext())) {
                    val intent = Intent(context, SettingActivity::class.java)
                    intent.putExtra(EXTRA_IS_LOCK_SCREEN, true)
                    intent.putExtra("move_activity", ActivityEnum.THEME_SETTING)
                    activityResultLauncher.launch(intent)

                    // Ga Log Event
                    GaIconClickEvent.logLockScreenThemeSettingIconClickEvent()
                }
            }

            time.apply {
                text = DateUtil.getNowDateString()
                setOnClickListener {
                    if (NetworkUtils.checkNetworkState(requireContext())) {
                        fetchThemeData()
                    }
                }
            }

            ivWeatherForecastClose.setOnClickListener {
                hideWeatherForecast()
                viewModel.dispatchEvent(ThemeUiEvent.CloseNews(NewsType.FORECAST))
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.simpleThemeData.collect { simpleThemeData ->
                    simpleThemeData?.data?.let { data ->
                        data.weatherInfo.let { weatherInfo ->
                            weatherInfo.nowWeather.let { weather ->
                                setWeatherInfo(weather)
                                setSunflower(weather)
                            }

                            showNotification(requireContext(), weatherInfo.nowWeather, weatherInfo.region)
                        }

                        campaignInfo = data.campaignInfo
                        dongDongInfo = data.mobonDongDongInfo

                        viewModel.dispatchEvent(ThemeUiEvent.UpdateThemeType)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.weatherForecastShown.collect {
                    refreshNews()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.newsContentsData.filterNotNull().first().let { data ->
                handleNewsContentsData(data.first, data.second)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.newsContentsData.collect { data ->
                    data?.let {
                        handleNewsContentsData(it.first, it.second)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentUserPoint.collect {
                    try {
                        PrefRepository.LockQuickInfo.currentUserPoint = it
                        with(viewDataBinding.topBar.tvUserPoint) {
                            post { text = CommonUtils.getCommaNumeric(it.toFloat()).plus("P") }
                        }
                    } catch (e: Exception) {
                        Logger.e(e.message)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.availablePoint.collect { point ->
                    try {
                        PrefRepository.LockQuickInfo.todayAvailablePoint = point
                        viewDataBinding.bottomArea.tvBubblepoint.text = CommonUtils.getCommaNumeric(point.toFloat()).plus("P")
                    } catch (e: Exception) {
                        Logger.e(e.message)
                    }
                }
            }
        }

        // 미션 체크 후 실행
        viewModel.participationCheck.observe(viewLifecycleOwner) {
            if (it != null) {
//                when (it) {
//                    0 -> {
//                        // 참여 가능한 미션 실행
//                        runWebView("", LockScreenWebViewActivity.ViewType.MISSION.name)
//                    }
//                    else -> {
//                        if (missionData!!.mission.size > missionIdx!!) {
//                            // 참여 가능한 미션이 아닌 경우 다음 미션 체크
//                            CustomToast.showToast(requireContext(), getString(R.string.toast_message_ended_mission_1))
//                            missionIdx = missionIdx!! + 1
//                            viewModel.participationCheck(missionData!!, missionIdx!!)
//                        } else {
//                            // 모든 미션이 참여 가능한 미션이 아닌 경우
//                            CustomToast.showToast(requireContext(), getString(R.string.toast_message_ended_mission_2))
//                        }
//                    }
//                }

                // 20241024: 참여 가능하지 않은 미션 포함 미션 실행시킴
                runWebView("", LockScreenWebViewActivity.ViewType.MISSION.name)

                showPoint()
            }
        }

        viewModel.resultNoticeDetail.observe(viewLifecycleOwner) {
            try {
                startFragment(
                    R.id.fragmentContainer, NoticeDetailFragment::class.java,
                    Intent().putExtra("title", it.title)
                        .putExtra("content", it.content)
                        .putExtra("createdAt", it.createdAt)
                        .putExtra("noticeId", it.noticeId)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.replayEffect.collect { effect ->
                    if (!effect.consumed) {
                        effect.markConsumed()

                        when (val data = effect.data) {
                            is ThemeBannerUiEffect.ShowMobWithScriptBanner -> {
                                bannerDelegate.showMobWithBannerByZoneId(data.zone, data.script)
                            }

                            is ThemeBannerUiEffect.ShowBottomBannerPoint -> {
                                if (data.isAvailable) {
                                    bannerDelegate.showBottomBannerPoint()
                                    viewDataBinding.tvBottomBannerPoint.text = "${data.point}P"
                                } else {
                                    bannerDelegate.hideBottomBannerPoint()
                                }
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { data ->
                    when (data) {
                        is ThemeUiEffect.RefreshBackgroundTheme -> {
                            changeBackground(data.themeType, data.weather)
                        }

                        is ThemeUiEffect.ShowEarnPointMessage -> {
                            showEarnPointMessage(data.point, data.isBottomBanner)
                        }

                        is ThemeUiEffect.ShowWarningNews -> {
                            showWarningDetail()
                        }

                        is ThemeUiEffect.ShowNoticeNews -> {
                            showNoticeDetail(data.noticeId)
                        }

                        is ThemeUiEffect.ShowNews -> {
                            showNewsDetail(
                                articleUrl = data.articleUrl,
                                guid = data.guid
                            )
                        }

                        is ThemeUiEffect.ShowCoupangCpsButton -> {
                            showCoupangCps()
                        }

                        is ThemeUiEffect.ShowLiveCampaignButton -> {
                            campaignData = data.liveCampaign
                            showLiveStreaming()
                        }

                        is ThemeUiEffect.ShowAutoMissionButton -> {
                            missionData = data.autoMission
                            showMission()
                        }

                        is ThemeUiEffect.ShowDongDongButton -> {
                            dongDongData = data.dongDong
                            showDongDong()
                        }

                        is ThemeUiEffect.ShowLandingEventButton -> {
                            landingEventInfo = data.landingEvent
                            showOfferwall()
                        }

                        is ThemeUiEffect.ShowPointButton -> {
                            showPoint()
                        }

                        is ThemeUiEffect.ShowNoPointAction -> {
                            showNoPointMessage()
                        }

                        is ThemeUiEffect.ShowEarnPointAction -> {
                            showEarnPointAction()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun setBannerDelegate() {
        bannerDelegate = LockScreenBannerDelegate.LockScreenBannerDelegateImpl(
            context = requireContext(),
            adpieView = viewDataBinding.adView,
            bannerContainer = viewDataBinding.bannerContainer,
            popupBannerContainer = viewDataBinding.clPopupBanner,
            bannerPointContainer = viewDataBinding.clBottomBannerPoint,
            dispatchEvent = viewModel::dispatchReplayEvent
        )
    }

    private fun setEventReceive() {
        eventReceive<Intent> { intent ->
            if (intent.action == Intent.ACTION_TIME_TICK) {
                viewDataBinding.time.text = DateUtil.getNowDateString()

                if (NetworkUtils.checkNetworkState(requireContext())) {
                    if (DateUtil.getNowMin() == 0 && DateUtil.getNowSecond() >= ThemeConstants.REFRESH_USER_POINT_DELAY_SEC) {
                        viewModel.dispatchEvent(ThemeUiEvent.RefreshUserPoint)
                    }
                }
            }
        }
    }

    /**
     * 테마 데이터를 갱신함
     * - 락스크린이 생성되었을 경우
     * - 락스크린이 유지되어 있는 상태에서 ScreenOn 되었을 경우
     *
     * @param fromCreated 락스크린이 생성되었는지 여부
     */
    private fun refreshData(fromCreated: Boolean): Boolean {
        if (!fromCreated && !requireActivity().isScreenOn()) {
            showPoint()
            return false
        }

        // 하단 1p 배너 포인트 갱신
        viewModel.dispatchEvent(ThemeUiEvent.CheckBottomBannerEarnPoint)

        // 락스크린 재사용 시 경우 예측 날씨를 보여줄 지 여부 체크
        viewModel.dispatchEvent(ThemeUiEvent.CheckWeatherForecastData)

        // update user point
        viewModel.dispatchEvent(ThemeUiEvent.RefreshUserPoint)

        // load location data
        fetchThemeData()

        // load banner data
        loadBanner(fromCreated)

        return true
    }

    private fun observeActivityViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                activityViewModel.replayEffect
                    .throttleFirst(FETCH_REFRESH_DATA_DELAY.milliseconds)
                    .collect { effect ->
                        val shouldHandle =
                            !effect.consumed || (effect.data is LockScreenUiEffect.RefreshData && (effect.data.fromCreated || effect.data.isThemeChange))

                        if (shouldHandle) {
                            when (val data = effect.data) {
                                is LockScreenUiEffect.RefreshData -> {
                                    val isRefreshed = refreshData(fromCreated = data.fromCreated)

                                    if (isRefreshed) {
                                        effect.markConsumed()
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }

    private fun changeBackground(type: ThemeType, weather: Weather) {
        setPlayerView(videoUri = weather.weatherVideo().toUri())
        setBackgroundImage(weather = weather)

        if (type == ThemeType.VIDEO) {
            playVideo()
        } else {
            pauseVideo()
        }
    }

    private fun handleNewsContentsData(type: NewsType, newsContents: Any) {
        if (type == NewsType.FORECAST) {
            hideNewsLayout()
            showWeatherForecast()
        } else {
            when (type) {
                NewsType.WARNING -> {
                    updateNews(
                        newsTitle = getString(R.string.news_warning),
                        newsContent = newsContents as String,
                        titleBackground = R.drawable.round_warning,
                        rightButton = R.drawable.btn_popup_close,
                        clickNews = { viewModel.dispatchEvent(ThemeUiEvent.SelectWarningNews) },
                        closeNews = { viewModel.dispatchEvent(ThemeUiEvent.CloseNews(NewsType.WARNING)) }
                    )
                }

                NewsType.NOTICE -> {
                    val notice = newsContents as LockNotice
                    updateNews(
                        newsTitle = getString(R.string.news_notice),
                        newsContent = notice.title ?: "",
                        titleBackground = R.drawable.round_notice,
                        rightButton = R.drawable.btn_popup_close,
                        clickNews = { viewModel.dispatchEvent(ThemeUiEvent.SelectNoticeNews(notice.noticeId ?: 0)) },
                        closeNews = { viewModel.dispatchEvent(ThemeUiEvent.CloseNews(NewsType.NOTICE)) }
                    )
                }

                else -> {
                    val news = newsContents as News
                    updateNews(
                        newsTitle = getString(R.string.news),
                        newsContent = news.title,
                        titleBackground = R.drawable.round_news,
                        rightButton = R.drawable.icon_arrow_gray,
                        clickNews = { viewModel.dispatchEvent(ThemeUiEvent.SelectNews(news.articleUrl, news.guid ?: "")) },
                        closeNews = { viewModel.dispatchEvent(ThemeUiEvent.CloseNews(NewsType.NEWS)) }
                    )
                }
            }
            hideWeatherForecast()
            showNewsLayout()
        }
    }

    private fun runWebView(url: String, type: String, guid: String? = "") {
        val intent = Intent(context, LockScreenWebViewActivity::class.java)
        intent.putExtra(KEY_LOAD_URL, url)
        intent.putExtra(KEY_VIEW_TYPE, type)

        when (type) {
            LockScreenWebViewActivity.ViewType.MISSION.name -> {
                if (missionData != null) {
                    intent.putExtra("missionData", missionData)
                    intent.putExtra("missionIdx", missionIdx)
                }
            }

            LockScreenWebViewActivity.ViewType.LIVE_STREAMING.name -> {
                if (campaignInfo != null) {
                    intent.putExtra("campaignInfo", campaignInfo)
                }
                if (campaignData != null) {
                    intent.putExtra("campaignData", campaignData)
                }
            }

            LockScreenWebViewActivity.ViewType.NEWS.name -> {
                viewModel.newsRewardInfo.value?.let {
                    intent.putExtra("newsPoint", it.point)
                    intent.putExtra("thresholdSec", it.thresholdSec)
                    intent.putExtra("isAvailable", it.isAvailable)
                }
                intent.putExtra("guid", guid)
            }

            LockScreenWebViewActivity.ViewType.DONG_DONG.name -> {
                if (dongDongInfo != null) {
                    intent.putExtra(KEY_DONG_DONG, dongDongInfo)
                }
            }
        }

        activityResultLauncher2.launch(intent)
    }

    private fun showEventButton(type: LockScreenActivity.ButtonType, onButtonShown: () -> Unit = { }) {
        with(viewDataBinding.bottomArea) {
            flowerLayout.isVisible = type == LockScreenActivity.ButtonType.POINT
            missionLayout.isVisible = type == LockScreenActivity.ButtonType.MISSION
            liveStreamingLayout.isVisible = type == LockScreenActivity.ButtonType.LIVE_STREAMING
            offerwallLayout.isVisible = type == LockScreenActivity.ButtonType.OFFERWALL
            flMobonDongDongLayout.isVisible = type == LockScreenActivity.ButtonType.DONG_DONG
            flCoupangCps.isVisible = type == LockScreenActivity.ButtonType.COUPANG_CPS
        }

        onButtonShown.invoke()

        with(viewDataBinding.bottomArea) {
            ivCoupangCps.clearAnimation()
            liveStreaming.clearAnimation()
            mission.clearAnimation()
            flDongDongButton.clearAnimation()
            offerwallIV.clearAnimation()

            val view = when (type) {
                LockScreenActivity.ButtonType.MISSION -> mission
                LockScreenActivity.ButtonType.LIVE_STREAMING -> liveStreaming
                LockScreenActivity.ButtonType.OFFERWALL -> offerwallIV
                LockScreenActivity.ButtonType.DONG_DONG -> flDongDongButton
                LockScreenActivity.ButtonType.COUPANG_CPS -> ivCoupangCps
                LockScreenActivity.ButtonType.POINT -> null
            }

            view?.startAnimation(animation)
        }
    }

    private fun showPoint() {
        showEventButton(LockScreenActivity.ButtonType.POINT)
    }

    private fun showMission() {
        showEventButton(LockScreenActivity.ButtonType.MISSION) {
            missionData?.let { data ->
                val point = data.mission[0].user_point!!.split(".")[0].toLong()
                viewDataBinding.bottomArea.tvMissionPoint.text = LockScreenActivity.pointFormat(point)
            }
        }
    }

    private fun showCoupangCps() {
        showEventButton(LockScreenActivity.ButtonType.COUPANG_CPS)
    }

    private fun showLiveStreaming() {
        showEventButton(LockScreenActivity.ButtonType.LIVE_STREAMING) {
            with(viewDataBinding.bottomArea) {
                // 광고주 로고
                val imgLogo = try {
                    if (StringUtils.isNotEmpty(campaignData?.data!![0].img_logo)) {
                        "https:" + campaignData?.data!![0].img_logo
                    } else {
                        ""
                    }
                } catch (e: Exception) {
                    ""
                }

                if (StringUtils.isNotEmpty(imgLogo)) {
                    Glide.with(requireContext()).load(imgLogo).into(adLogoIV)
                    adLogoCV.visibility = View.VISIBLE
                } else {
                    adLogoCV.visibility = View.GONE
                }

                // 지급 포인트
                tvLiveStreamingPoint.text =
                    LockScreenActivity.pointFormat(campaignInfo?.point!!.toLong())
            }
        }
    }

    private fun showOfferwall() {
        showEventButton(LockScreenActivity.ButtonType.OFFERWALL) {
            landingEventInfo?.let { info ->
                val landingImage = OfferwallEnum.getLandingImage(info.landingEventId)
                Glide.with(requireContext()).load(landingImage).into(viewDataBinding.bottomArea.offerwallIV)
            }
        }
    }

    private fun showDongDong() {
        showEventButton(LockScreenActivity.ButtonType.DONG_DONG) {
            with(viewDataBinding.bottomArea) {
                dongDongData?.let { data ->
                    val imgLogo = "https:${data.img_logo}"
                    Glide.with(requireContext())
                        .load(imgLogo)
                        .error(R.drawable.ic_default_dongdong_logo)
                        .into(ivMobonDongDongLogo)

                    tvMobonDongDongTitle.text = data.site_title ?: "AD"
                }
            }
        }
    }

    private fun showNoPointMessage() {
        with(viewDataBinding.bottomArea) {
            toastPoint.visibility = View.GONE
            toastNoPoint.visibility = View.VISIBLE

            Timer().schedule(object : TimerTask() {
                override fun run() {
                    toastNoPoint.post {
                        toastNoPoint.visibility = View.INVISIBLE
                    }
                }
            }, 2000)
        }
    }

    private fun showEarnPointAction() {
        with(viewDataBinding.bottomArea) {
            toastPoint.visibility = View.VISIBLE
            toastNoPoint.visibility = View.INVISIBLE

            coin.playAnimation()
        }

        CommonUtils.vibrate(requireContext())
        LockScreenActivity.playSound()

        bannerDelegate.clickForBannerDialog()
    }

    private fun initBottomLayoutViews() {
        viewDataBinding.bottomArea.apply {
            flower.setOnSingleClickListener {
                viewModel.dispatchEvent(ThemeUiEvent.SavePoint(1))

                // Ga Log Event
                GaButtonClickEvent.logLockScreenRewardSunflowerButtonClickEvent()
            }

            mission.setOnSingleClickListener {
                viewModel.participationCheck(missionData!!, missionIdx!!)

                // Ga Log Event
                GaAdButtonClickEvent.logAdButtonPomissionClickEventCoupang()
            }

            liveStreaming.setOnSingleClickListener {
                var url = try {
                    (UrlHelper.MOBON_DOMAIN + campaignData?.data!![0].purl) ?: ""
                } catch (e: Exception) {
                    ""
                }

                runWebView(url, LockScreenWebViewActivity.ViewType.LIVE_STREAMING.name)
                showPoint()

                // Ga Log Event
                GaAdButtonClickEvent.logAdButtonMobonLiveClickEventCoupang()
            }

            offerwallIV.setOnSingleClickListener {
                viewModel.onClickOfferwall()
                showPoint()
            }

            flDongDongButton.setOnSingleClickListener {
                if (requireActivity().hasUsageStatsPermission()) {
                    startDongDong()
                    showPoint()
                } else {
                    popupUsageStatsPermission()
                }

                // Ga Log Event
                GaAdButtonClickEvent.logAdButtonMobonDongdongClickEventCoupang()
            }

            ivCoupangCps.setOnSingleClickListener {
                viewModel.onClickCoupangCps()
                showPoint()

                // Ga Log Event
                GaAdButtonClickEvent.logAdButtonCoupangCpsClickEventCoupang()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.dispatchEvent(ThemeUiEvent.FetchRemoteConfigData)
    }

    override fun onResume() {
        super.onResume()

        viewModel.dispatchEvent(ThemeUiEvent.UpdateThemeType)
    }

    private fun showEarnPointMessage(point: Int, isBottomBanner: Boolean) {
        if (PrefRepository.LockQuickInfo.isShowEarnPointMessage) {
            PrefRepository.LockQuickInfo.isShowEarnPointMessage = false

            val msg = String.format(getString(R.string.toast_bottom_banner_point_message), point)
            CustomToast.showToast(requireContext(), msg)

            if (isBottomBanner) {
                bannerDelegate.hideBottomBannerPoint()
            }
        }
    }

    private fun showWeatherForecast() {
        viewDataBinding.clWeatherForecastLayout.visibility = View.VISIBLE
    }

    private fun hideWeatherForecast() {
        viewDataBinding.clWeatherForecastLayout.visibility = View.GONE
    }

    private fun hideNewsLayout() {
        with(viewDataBinding) {
            clNewsLayout.visibility = View.GONE
            vNewsButton.visibility = View.GONE
        }
    }

    private fun showNewsLayout() {
        with(viewDataBinding) {
            clNewsLayout.visibility = View.VISIBLE
            vNewsButton.visibility = View.VISIBLE
        }
    }

    private fun showWarningDetail() {
        runWebView(
            "https://weather.naver.com/warning",
            LockScreenWebViewActivity.ViewType.NORMAL.name
        )
    }

    private fun showNoticeDetail(noticeId: Int) {
        viewModel.connectNoticeDetail(noticeId)
    }

    private fun showNewsDetail(articleUrl: String, guid: String) {
        runWebView(
            url = articleUrl,
            guid = guid,
            type = LockScreenWebViewActivity.ViewType.NEWS.name
        )
    }

    private fun refreshNews() {
        viewModel.dispatchEvent(ThemeUiEvent.UpdateNews)
    }

    private fun updateNews(
        newsTitle: String,
        newsContent: String,
        @DrawableRes titleBackground: Int,
        @DrawableRes rightButton: Int,
        clickNews: () -> Unit,
        closeNews: () -> Unit,
    ) {
        viewDataBinding.apply {
            title.text = newsTitle
            title.setBackgroundResource(titleBackground)
            content.text = newsContent
            newsBtn.setImageResource(rightButton)
            clNewsLayout.setOnClickListener { clickNews() }
            vNewsButton.setOnClickListener { closeNews() }
        }
    }

    private fun fetchThemeData() {
        if (!NetworkUtils.checkNetworkState(requireContext())) return

        val themeType = ThemeType.fromInt(PrefRepository.SettingInfo.selectThemeType)
        viewModel.dispatchEvent(ThemeUiEvent.FetchThemeData(themeType = themeType))
    }

    private fun loadBanner(fromCreated: Boolean) {
        if (!fromCreated || TextUtils.isEmpty(lastLoadBannerTimeOnCreate) || DateUtil.intervalBetweenDateText(lastLoadBannerTimeOnCreate) >= ThemeConstants.LOAD_BANNER_INTERVAL_MIN) {
            if (fromCreated) {
                lastLoadBannerTimeOnCreate = DateUtil.getTime()
            }

            bannerDelegate.loadAdBanner()
        }
    }

    /**
     * 선택된 테마 설정 (배경형, 영상형)
     */
    private fun setPlayerView(videoUri: Uri) {
        viewDataBinding.bgPlayerView.visibility = View.VISIBLE

        setExoPlayer(videoUri)
    }

    /**
     * @param weather
     */
    private fun setWeatherInfo(weather: Weather) {
        viewDataBinding.apply {
            tvSkyDesc.text = weather.skyDescription
            tvCurTemp.text = weather.temp.toString().plus(getString(R.string.degrees))
            tvWeatherDesc.text = weather.lockCondition()
            tvApparentTemp.text = weather.apparentTemp.toString().plus(getString(R.string.degrees))
            tvHumidity.text = weather.humidity.toString().plus(getString(R.string.percent))
        }
    }

    /**
     * 해바라기 버튼 설정
     * @param weather
     */
    private fun setSunflower(weather: Weather) {
        try {
            isSunrise = WeatherUtils.checkSunrise(weather)
            skyCode = weather.skyCode

            viewDataBinding.bottomArea.apply {
                if (isSunrise!!) {
                    flower.setAnimation(R.raw.basic)
                    sleepIV.visibility = View.GONE
                    flowerMessageTV.text = resources.getString(R.string.no_point_message)
                } else {
                    flower.setImageDrawable(resources.getDrawable(R.drawable.ic_sleep_sunflower, null))
                    sleepIV.visibility = View.VISIBLE
                    flowerMessageTV.text = resources.getString(R.string.toast_message_see_you_tomorrow)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 영상 재생
     * @param weather
     */
    private fun playVideo() {
        viewDataBinding.bgPlayerView.videoSurfaceView?.invalidate()
        exoPlayer?.playVideo()
    }

    private fun pauseVideo() {
        exoPlayer?.pauseVideo()
    }

    /**
     * ExoPlayer Settings
     */
    private fun setExoPlayer(videoUri: Uri) {
        viewDataBinding.apply {
            if (exoPlayer == null) {
                exoPlayer = requireContext().createExoPlayer()
            }

            exoPlayer?.run {
                prepareVideo(videoUri)

                setPlaybackParameters(PlaybackParameters(PLAYER_SPEED))

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_READY -> {
                                if (videoFormat != null) {
                                    bgPlayerView.visibility = View.VISIBLE
                                    bgImageView.visibility = View.GONE
                                }
                            }

                            Player.STATE_ENDED -> {
                                playVideo()
                            }
                        }
                    }
                })
            }

            bgPlayerView.apply {
                player = exoPlayer
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                setKeepContentOnPlayerReset(true)
            }
        }
    }

    /**
     * Background Image 설정
     * @param weather
     */
    private fun setBackgroundImage(weather: Weather) {
        viewDataBinding.bgImageView.visibility = View.VISIBLE

        // 날씨 코드 구분을 배경은 7개, 영상은 4개로 하여
        // 영상 재생 전 보여줄 이미지 설정을 영상 썸네일로 함
        Glide.with(requireContext())
            .load(weather.weatherVideo().toUri())
            .frame(0)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(viewDataBinding.bgImageView)
    }

    private fun startDongDong() {
        val intent = Intent(requireContext(), OverlayProgressService::class.java)
        requireContext().stopService(intent.apply {
            action = OverlayProgressService.ACTION_STOP_OVERLAY
        })

        dongDongData?.let { data ->
            val url = UrlHelper.MOBON_DOMAIN + data.purl
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

            requireContext().startService(intent.apply {
                action = OverlayProgressService.ACTION_START_OVERLAY
                putExtra(KEY_DONG_DONG, dongDongInfo)
            })
        }
    }

    private fun popupUsageStatsPermission() {
        val str = resources.getString(R.string.popup_usage_stats_permission)
        val spannableString = SpannableString(str)
        val builder = SpannableStringBuilder(spannableString)

        val begin = builder.indexOf("사")
        val end = builder.lastIndexOf("을")
        builder.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.default_main_color)),
            begin,
            end,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        val begin2 = builder.indexOf("(")
        val end2 = builder.lastIndex + 1
        builder.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.grey_74)),
            begin2,
            end2,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        HCCommonDialog(requireContext()).apply {
            setDialogType(DialogType.ALERT)
            setLayout(R.layout.popup_overlay)
            setContent(builder)
            setPositiveButtonText(R.string.setting)
            setNegativeButtonText(R.string.popup_usage_stats_permission_negative_button)
            setOnDismissListener(object : HCCommonDialog.OnDismissListener {
                override fun onDismiss(menuId: Int) {
                    if (menuId == DialogType.BUTTON_POSITIVE.ordinal) {
                        val intent = if (Build.VERSION.SDK_INT in Build.VERSION_CODES.Q..Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            val uri = Uri.fromParts("package", requireContext().packageName, null)
                            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, uri)
                        } else {
                            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        }
                        requestOverlayPermission.launch(intent)
                    }
                }
            })
            setOnCancelListener {
                showPoint()
            }
            show()
        }
    }

    override fun onPause() {
        super.onPause()
        pauseVideo()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()

        bannerDelegate.releaseAdBanner()

        exoPlayer?.release()
        exoPlayer = null
    }

    private val requestOverlayPermission = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (requireActivity().hasUsageStatsPermission()) {
            startDongDong()
            showPoint()
        } else {
            CustomToast.showToast(requireContext(), resources.getString(R.string.toast_usage_stats_permission))
        }
    }

    private var activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (RESULT_OK == result.resultCode) {
            val isThemeChange = result.data?.getBooleanExtra("isThemeChange", false) ?: false
            PrefRepository.LockQuickInfo.isThemeChange = isThemeChange
        }
    }

    private var activityResultLauncher2 = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (RESULT_OK == result.resultCode) {
            var type = result.data?.getStringExtra("type")
            when (type) {
                LockScreenWebViewActivity.ViewType.NEWS.name -> {
                    var newsPoint = try {
                        result.data?.getIntExtra("newsPoint", 0)
                    } catch (e: Exception) {
                        0
                    }

                    watchedNews = try {
                        result.data?.getStringExtra("watchedNews")
                    } catch (e: Exception) {
                        ""
                    }

                    try {
                        newsPoint?.let { point ->
                            if (point > 0) {
                                viewModel.dispatchEvent(ThemeUiEvent.SavePoint(point))
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                LockScreenWebViewActivity.ViewType.LIVE_STREAMING.name -> {
                    try {
                        val point = campaignInfo?.point ?: (viewModel.campaignInfo.value?.point ?: 0)

                        viewModel.dispatchEvent(ThemeUiEvent.SavePoint(point))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                LockScreenWebViewActivity.ViewType.MISSION.name -> {
                    missionData?.let { data ->
                        val point = (data.mission[0].user_point ?: "").split(".")[0]
                        if (point.isNotEmpty()) {
                            viewModel.currentUserPoint.value?.let { it.plus(point.toInt()) }
                            CustomToast.showToast(requireContext(), "${point}P 적립 완료되었습니다")
                        }
                    }
                }

                LockScreenWebViewActivity.ViewType.ANIC_GAME.name -> {
                    val gamePoint = try {
                        result.data?.getIntExtra("gamePoint", 0)
                    } catch (e: Exception) {
                        0
                    }

                    gamePoint?.let { point ->
                        if (point > 0) {
                            viewModel.dispatchEvent(ThemeUiEvent.SavePoint(point))
                        }
                    }
                }
            }
        }
    }
}