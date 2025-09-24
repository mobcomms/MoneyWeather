package com.moneyweather.ui.lockscreen

import android.content.Context
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import com.gomfactory.adpie.sdk.AdView
import com.gomfactory.adpie.sdk.NativeAd
import com.gomfactory.adpie.sdk.nativeads.NativeAdView
import com.gomfactory.adpie.sdk.nativeads.NativeAdViewBinder
import com.mobwith.sdk.MobwithBannerView
import com.mobwith.sdk.callback.iBannerCallback
import com.moneyweather.BuildConfig
import com.moneyweather.R
import com.moneyweather.event.theme.ThemeBannerUiEvent
import com.moneyweather.extensions.applyAdBannerSettings
import com.moneyweather.extensions.applyBottomAdBannerSettings
import com.moneyweather.extensions.toPx
import com.moneyweather.model.enums.BannerType
import com.moneyweather.ui.dialog.AdBannerDialog
import com.moneyweather.util.AdRatioScheduler
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.PrefRepository
import com.moneyweather.util.analytics.GaAdBannerClickEvent
import com.moneyweather.util.webview.BannerWebViewClient
import timber.log.Timber

interface LockScreenBannerDelegate {

    fun loadAdBanner()
    fun releaseAdBanner()

    fun clickForBannerDialog()
    fun showMobWithBannerByZoneId(zone: String, script: String)
    fun showMobWithBannerFromSDK()

    fun showBottomBannerPoint()
    fun hideBottomBannerPoint()

    class LockScreenBannerDelegateImpl(
        val context: Context,
        private val adpieView: AdView,
        private val bannerContainer: ConstraintLayout,
        val popupBannerContainer: ConstraintLayout? = null,
        val bannerPointContainer: ConstraintLayout? = null,
        val dispatchEvent: (ThemeBannerUiEvent) -> Unit,
    ) : LockScreenBannerDelegate {

        private var nativeAd: NativeAd? = null
        private var adBannerDialog: AdBannerDialog? = null

        private var clickCountForBanner: Int = 0

        private fun showAdPieBanner() {
            adpieView.visibility = View.VISIBLE
        }

        private fun hideAdPieBanner() {
            adpieView.visibility = View.GONE
        }

        private fun alignMobWithSdkBanner(view: View) {
            view.layoutParams = LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomToBottom = LayoutParams.PARENT_ID
                startToStart = LayoutParams.PARENT_ID
                endToEnd = LayoutParams.PARENT_ID
            }
        }

        private fun showBannerContainer() {
            bannerContainer.visibility = View.VISIBLE
        }

        private fun hideBannerContainer() {
            bannerContainer.visibility = View.GONE
        }

        private fun replaceBanner(view: View) {
            bannerContainer.removeAllViews()
            bannerContainer.addView(view)
        }

        private fun fetchMobWithCoupangScript320X50() {
            dispatchEvent(
                ThemeBannerUiEvent.FetchMobWithScriptBanner(
                    zoneId = BuildConfig.MOBWITH_ZONE_ID_320_50_COUPANG,
                    width = 320,
                    height = 50
                )
            )
        }

        private fun fetchMobWithScript320X50() {
            dispatchEvent(
                ThemeBannerUiEvent.FetchMobWithScriptBanner(
                    zoneId = BuildConfig.MOBWITH_ZONE_ID_320_50,
                    width = 320,
                    height = 50
                )
            )
        }

        private fun fetchMobWithScript300X250() {
            dispatchEvent(
                ThemeBannerUiEvent.FetchMobWithScriptBanner(
                    zoneId = BuildConfig.MOBWITH_ZONE_ID_300_250,
                    width = 300,
                    height = 250
                )
            )
        }

        override fun loadAdBanner() {
            val type = AdRatioScheduler.getBannerType()
            when (BannerType.fromType(type)) {
                BannerType.ADPIE -> {
                    if (AdRatioScheduler.isAdPieNativeFirst()) {
                        loadAdPieNative()
                    } else {
                        loadAdPieBanner()
                    }
                }

                BannerType.MOBWITH -> {
                    if (IS_SDK_BASED) {
                        showMobWithBannerFromSDK()
                    } else {
                        fetchMobWithScript320X50()

                        FirebaseAnalyticsManager.logEvent(FirebaseAnalyticsManager.CALL, Bundle().apply {
                            putString(FirebaseAnalyticsManager.REQUEST_API, BannerType.MOBWITH.type)
                        })
                    }
                }

                else -> {}
            }
        }

        override fun releaseAdBanner() {
            releaseAdPieNative()
        }

        /**
         * 애드파이 배너 광고
         */
        private fun loadAdPieBanner() {
            try {
                adpieView.apply {
                    slotId = BuildConfig.ADPIE_BANNER_SLOT_ID
                    setScaleUp(true)
                    setAdListener(object : AdView.AdListener {
                        override fun onAdLoaded() {
                            Timber.tag(TAG).d("AdPie Banner onLoaded")
                            // 광고 표출 성공 후 이벤트 발생
                            showAdPieBanner()
                            hideBannerContainer()

                            fetchBottomBannerPointAvailable()
                            setBottomBannerPointLayout(false)
                        }

                        override fun onAdFailedToLoad(errorCode: Int) {
                            Timber.tag(TAG).d("AdPie Banner onFailed")
                            // 광고 요청 또는 표출 실패 후 이벤트 발생
                            hideAdPieBanner()
                            handleAdPieBannerFailed()
                        }

                        override fun onAdClicked() {
                            Timber.tag(TAG).d("AdPie Banner onAdClicked")
                            // 광고 클릭 후 이벤트 발생
                            clickBottomBannerAd()

                            // Ga Log Event
                            GaAdBannerClickEvent.logLockScreenAdpieBannerClickEvent()
                        }
                    })
                }.run {
                    load()
                }
            } catch (e: Exception) {
                Timber.tag(TAG).d("AdPie Banner exception : ${e.message}")
                hideAdPieBanner()
                handleAdPieBannerFailed()

                e.printStackTrace()
            }
        }

        /**
         * 애드파이 네이티브 광고
         */
        private fun loadAdPieNative() {
            try {

                val viewBinder = NativeAdViewBinder.Builder(R.layout.adpie_native_ad_template)
                    .setIconImageId(R.id.native_ad_icon)
                    .setTitleId(R.id.native_ad_title)
                    .setOptOutId(R.id.native_optout)
                    .build()

                nativeAd = NativeAd(
                    context,
                    BuildConfig.ADPIE_NATIVE_SLOT_ID,
                    viewBinder
                )

                nativeAd?.let {
                    it.loadAd()

                    it.adListener = object : NativeAd.AdListener {
                        override fun onAdLoaded(nativeAdView: NativeAdView) {
                            Timber.tag(TAG).d("AdPie Native onLoaded")
                            // 광고 로딩 완료 후 이벤트 발생
                            // 광고 요청 후 즉시 노출하고자 할 경우
                            showBannerContainer()
                            replaceBanner(nativeAdView)

                            hideAdPieBanner()

                            fetchBottomBannerPointAvailable()
                            setBottomBannerPointLayout(true)
                        }

                        override fun onAdFailedToLoad(errorCode: Int) {
                            Timber.tag(TAG).d("AdPie Native onFailed")
                            // 광고 요청 또는 표출 실패 후 이벤트 발생
                            handleAdPieNativeFailed()
                        }

                        override fun onAdShown() {
                            // 광고 표출 후 이벤트 발생
                        }

                        override fun onAdClicked() {
                            Timber.tag(TAG).d("AdPie Native onAdClicked")
                            // 광고 클릭 후 이벤트 발생
                            clickBottomBannerAd()

                            // Ga Log Event
                            GaAdBannerClickEvent.logLockScreenAdpieBannerClickEvent()
                        }
                    }
                }
            } catch (e: Exception) {
                handleAdPieNativeFailed()

                e.printStackTrace()
            }
        }

        private fun releaseAdPieNative() {
            if (nativeAd != null) {
                nativeAd?.destroy()
                nativeAd = null
            }
        }

        private fun handleAdPieBannerFailed() {
            if (AdRatioScheduler.isAdPieNativeFirst()) {
                // Native(First) -> Banner(Current) -> Script(Request)
                fetchMobWithCoupangScript320X50()
            } else {
                // Banner(First and Current) -> Native(Request)
                loadAdPieNative()
            }
        }

        private fun handleAdPieNativeFailed() {
            if (AdRatioScheduler.isAdPieNativeFirst()) {
                // Native(First and Current) -> Banner(Request)
                loadAdPieBanner()
            } else {
                // Banner(First) -> Native(Current) -> Script(Request)
                fetchMobWithCoupangScript320X50()
            }
        }

        override fun showMobWithBannerByZoneId(zoneId: String, script: String) {
            when (zoneId) {
                BuildConfig.MOBWITH_ZONE_ID_320_50_COUPANG -> {
                    // 락스크린 화면 하단 띠배너 (쿠팡 전용)
                    if (script.isEmpty()) {
                        Timber.tag(TAG).d("MobWith cp onFailed")
                        fetchMobWithCoupangScript320X50()
                    } else {
                        Timber.tag(TAG).d("MobWith cp onLoaded")
                        showMobWithBanner(script)
                    }
                }

                BuildConfig.MOBWITH_ZONE_ID_320_50 -> {
                    // 락스크린 화면 하단 띠배너
                    if (script.isEmpty() || script.contains(NO_AD_SCRIPT)) {
                        fetchMobWithCoupangScript320X50()
                    } else {
                        showMobWithBanner(script)
                    }
                }

                BuildConfig.MOBWITH_ZONE_ID_300_250 -> {
                    // 기본 포인트 클릭시 뜨는 팝업 광고
                    createPopupBanner(script)
                }
            }
        }

        /**
         * 모비위드 배너
         *
         * @param bannerScript
         */
        private fun showMobWithBanner(bannerScript: String) {
            try {
                val webView = WebView(context).applyBottomAdBannerSettings(
                    client = BannerWebViewClient(
                        onUrlLoading = { _, _ ->
                            clickBottomBannerAd()

                            // Ga Log Event
                            GaAdBannerClickEvent.logLockScreenMobwithClickEvent()
                        },
                        onFinished = { _, _ -> }
                    )
                )

                webView.loadDataWithBaseURL(
                    null,
                    bannerScript,
                    "text/html",
                    "utf-8",
                    null
                )

                showBannerContainer()
                replaceBanner(webView)
                hideAdPieBanner()

                fetchBottomBannerPointAvailable()
                setBottomBannerPointLayout(false)
            } catch (e: Exception) {
                fetchMobWithCoupangScript320X50()

                e.printStackTrace()
            }
        }

        /**
         * 모비위드 SDK 배너
         */
        override fun showMobWithBannerFromSDK() {
            var mobWithSDKBanner = MobwithBannerView(context).setBannerUnitId(BuildConfig.MOBWITH_UNIT_ID_320_50)

            // 하우스 배너 사용 설정
            mobWithSDKBanner.setUseHouseBanner(true)

            mobWithSDKBanner.setAdListener(object : iBannerCallback {
                override fun onLoadedAdInfo(result: Boolean, errorCode: String) {
                    if (result) {
                        Timber.tag(TAG).d("MobWithSDK Banner onLoaded")
                        alignMobWithSdkBanner(mobWithSDKBanner)

                        showBannerContainer()
                        replaceBanner(mobWithSDKBanner)
                        hideAdPieBanner()

                        fetchBottomBannerPointAvailable()
                        setBottomBannerPointLayout(false)
                    } else {
                        Timber.tag(TAG).d("MobWithSDK Banner onFailed")
                        mobWithSDKBanner.destroyAd()
                        mobWithSDKBanner = null

                        fetchMobWithCoupangScript320X50()
                    }
                }

                override fun onAdClicked() {
                    Timber.tag(TAG).d("MobWithSDK Banner onAdClicked")
                    clickBottomBannerAd()

                    // Ga Log Event
                    GaAdBannerClickEvent.logLockScreenMobwithClickEvent()
                }
            })

            mobWithSDKBanner.loadAd()
        }

        override fun showBottomBannerPoint() {
            bannerPointContainer?.visibility = View.VISIBLE
        }

        override fun hideBottomBannerPoint() {
            bannerPointContainer?.visibility = View.GONE
        }

        private fun fetchBottomBannerPointAvailable() {
            dispatchEvent(ThemeBannerUiEvent.FetchBottomBannerPointAvailable)
        }

        private fun setBottomBannerPointLayout(isNativeAd: Boolean) {
            bannerPointContainer?.run {
                layoutParams = (layoutParams as LayoutParams).apply {
                    endToEnd = LayoutParams.PARENT_ID
                    bottomToBottom = LayoutParams.PARENT_ID
                    rightMargin = 10.toPx()
                    bottomMargin = if (isNativeAd) {
                        73.toPx()
                    } else {
                        48.toPx()
                    }
                }
            }
        }

        private fun clickBottomBannerAd() {
            if (View.VISIBLE == bannerPointContainer?.visibility) {
                PrefRepository.LockQuickInfo.bottomBannerClickedTime = System.currentTimeMillis()
            }
        }

        /**
         * 기본 포인트 적립시 일정 클릭 수 마다
         * 모비위드 광고 배너를 띄운다 (300 * 250)
         */
        override fun clickForBannerDialog() {
            if (++clickCountForBanner == 1) {
                fetchMobWithScript300X250()
            } else if (clickCountForBanner >= PrefRepository.LockQuickInfo.bannerExposureCount) {
                showPopupBanner()

                clickCountForBanner = 0
            }
        }

        /**
         * 모비위드 광고 배너
         * TODO : 사용하지 않을 시 삭제
         *
         * @param script
         */
        private fun showBannerDialog(
            script: String,
            width: Int,
            height: Int
        ) {
            if (adBannerDialog != null) {
                adBannerDialog?.dismissDialog()
                adBannerDialog = null
            }

            adBannerDialog = AdBannerDialog(
                context = context,
                bannerWidth = width,
                bannerHeight = height
            )
            adBannerDialog?.apply {
                setOnClickListener(object : AdBannerDialog.OnClickListener {
                    override fun onClose() {
                        dismissDialog()
                    }
                })
            }?.run {
                val webView = WebView(context).applyAdBannerSettings(
                    client = BannerWebViewClient()
                )

                webView.loadDataWithBaseURL(
                    null,
                    script,
                    "text/html",
                    "utf-8",
                    null
                )

                addView(webView)

                showDialog()
            }
        }

        private fun createPopupBanner(
            script: String
        ) {
            popupBannerContainer?.let {
                val container = it.findViewById<FrameLayout>(R.id.fl_popup_banner_container)
                val close = it.findViewById<ImageView>(R.id.iv_popup_close)

                close.setOnClickListener {
                    hidePopupBanner()
                }

                val webView = WebView(context).applyAdBannerSettings(
                    client = BannerWebViewClient()
                )

                webView.loadDataWithBaseURL(
                    null,
                    script,
                    "text/html",
                    "utf-8",
                    null
                )

                container.addView(webView)
            }
        }

        private fun showPopupBanner() {
            popupBannerContainer?.apply {
                visibility = View.VISIBLE
            }
        }

        private fun hidePopupBanner() {
            popupBannerContainer?.apply {
                visibility = View.INVISIBLE
            }
        }
    }

    companion object {
        const val NO_AD_SCRIPT = "no ad"
        const val IS_SDK_BASED = true
        const val POINT_EARN_TIME = 5000

        const val TAG = "LockScreenBanner"
    }
}