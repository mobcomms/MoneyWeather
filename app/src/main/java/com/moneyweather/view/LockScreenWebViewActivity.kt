package com.moneyweather.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.enliple.datamanagersdk.ENDataManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.data.remote.response.AdNonSDKMobileBannerResponse
import com.moneyweather.data.remote.response.AutoMissionResponse
import com.moneyweather.data.remote.response.LockScreenResponse
import com.moneyweather.databinding.ActivityLockscreenWebviewBinding
import com.moneyweather.model.enums.DialogType
import com.moneyweather.ui.dialog.HCCommonDialog
import com.moneyweather.ui.dialog.ProgressDialog
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.CustomToast
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.Logger
import com.moneyweather.util.PrefRepository
import com.moneyweather.util.webview.WebViewUtils
import com.moneyweather.viewmodel.LockScreenWebViewViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class LockScreenWebViewActivity : BaseKotlinActivity<ActivityLockscreenWebviewBinding, LockScreenWebViewViewModel>() {

    override val layoutResourceId: Int get() = R.layout.activity_lockscreen_webview
    override val viewModel: LockScreenWebViewViewModel by viewModels()

    private lateinit var loadUrl: String
    private lateinit var viewType: String

    private var countDownTimer: CountDownTimer? = null
    private var dialog: ProgressDialog? = null
    private var campaignInfo: LockScreenResponse.CampaignInfo? = null
    private var campaignData: AdNonSDKMobileBannerResponse.Client? = null
    private var missionData: AutoMissionResponse? = null
    private var guid: String? = ""
    private var timeRemaining: Long = 0
    private var missionIdx: Int = 0
    private var newsPoint: Int = 1
    private var newsThresholdSec: Int = 0
    private var isNewsReward = false
    private var isAvailable: Boolean = false
    private var isTimerFinish: Boolean = false
    private var isScrolling: Boolean = false
    private var isStopMission: Boolean = false

    enum class ViewType {
        NORMAL, MISSION, NEWS, SHOP_PLUS, LIVE_STREAMING, ANIC_GAME, DONG_DONG, POMISSION_ZONE
    }

    companion object {
        const val BRIDGE_NAME = "HybridApp"
        const val KEY_LOAD_URL = "load_url"
        const val KEY_VIEW_TYPE = "view_type"
        const val KEY_DONG_DONG = "dong_dong"
        private const val BASE_URL = "https://weather.naver.com/"
        private const val PIC_WIDTH = 360.0
        private const val PERCENT = 100.0
        private const val FINISH_INTERVAL_TIME = 2_000
        const val DAILY_NEWS_REWARD_LIMIT = 10
        const val HOURLY_NEWS_REWARD_LIMIT = 2
        private var backPressedTime: Long = 0

        private fun getScale(context: Context): Int {
            val width = CommonUtils.getScreenWidth(context)
            var scale = (width / PIC_WIDTH) * PERCENT
            return scale.toInt()
        }
    }

    private val mWebViewClient: WebViewClient = object : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            onWebViewPageStartedWithTune720(view, url)
            super.onPageStarted(view, url, favicon)

            when (viewType) {
                ViewType.MISSION.name -> {
                    doPageStarted(view)
                }

                ViewType.LIVE_STREAMING.name -> {
                    startTimer()
                }
            }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url.toString()

            if (url.startsWith("tel://")) {
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
                    view?.context?.startActivity(intent)
                } catch (e: Exception) {
                    Logger.e(e.message)
                }
                return true
            } else if (url.startsWith("mailto://") || url.startsWith("sms://")) {
                try {
                    val intent = Intent(Intent.ACTION_SEND, Uri.parse(url)).apply {
                        type = "text/plain"
                    }
                    view?.context?.startActivity(intent)
                } catch (e: Exception) {
                    Logger.e(e.message)
                }
                return true
            } else if (WebViewUtils.isSchemeUrl(url)) {
                view?.let {
                    WebViewUtils.launchIntentScheme(it, url)
                }
                return true
            } else if (!url.contains("app.shoplus.io") && ViewType.SHOP_PLUS.name == viewType) {
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    view?.context?.startActivity(this)
                }
                return true
            }

            return false
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {

            url?.let {
                if (url.startsWith("tel://")) {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
                        view?.context?.startActivity(intent)
                    } catch (e: Exception) {
                        Logger.e(e.message)
                    }
                    return true
                } else if (url.startsWith("mailto://") || url.startsWith("sms://")) {
                    try {
                        val intent = Intent(Intent.ACTION_SEND, Uri.parse(url)).apply {
                            type = "text/plain"
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        Logger.e(e.message)
                    }
                    return true
                } else if (WebViewUtils.isSchemeUrl(url)) {
                    view?.let {
                        WebViewUtils.launchIntentScheme(it, url)
                    }
                    return true
                } else if (!url.contains("app.shoplus.io") && ViewType.SHOP_PLUS.name == viewType) {
                    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        view?.context?.startActivity(this)
                    }
                    return true
                }
            }

            return false
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            onWebViewPageFinishedWithTune720(view, url)
            super.onPageFinished(view, url)
        }
    }

    private val scrollChangeListener = View.OnScrollChangeListener { _, _, _, _, _ ->
        when (viewType) {
            ViewType.NORMAL.name -> {}

            ViewType.MISSION.name -> {}

            ViewType.NEWS.name -> {
                if (isAvailable && isNewsReward && !isScrolling && !isTimerFinish) {
                    isScrolling = true
                    startTimer() // 스크롤 시작시 타이머 시작

                    // 스크롤 감지 타이머 (스크롤 중단 여부 체크)
//                    viewDataBinding.webView.apply {
//                        removeCallbacks(stopScrollingRunnable)
//                        postDelayed(stopScrollingRunnable, 300)
//                    }
                }
            }

            ViewType.SHOP_PLUS.name -> {}
        }
    }

    override fun initStartView() {
        viewDataBinding.vm = viewModel

        try {
            super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        } catch (e: Exception) {
            Logger.e(e.message)
        }

        initFromIntent(intent)
        setWebView()
        observeViewModel()

        onBackPressedDispatcher.addCallback(this@LockScreenWebViewActivity, onBackPressedCallback)
    }

    private fun initFromIntent(intent: Intent?) {
        intent?.let {
            loadUrl = try {
                it.getStringExtra(KEY_LOAD_URL).toString()
            } catch (e: Exception) {
                BASE_URL
            }

            viewType = try {
                it.getStringExtra(KEY_VIEW_TYPE).toString()
            } catch (e: Exception) {
                ViewType.NORMAL.name
            }

            when (viewType) {
                ViewType.MISSION.name -> {
                    isStopMission = false

                    missionData = try {
                        it.getParcelableExtra("missionData")
                    } catch (e: Exception) {
                        null
                    }

                    missionIdx = try {
                        it.getIntExtra("missionIdx", 0)
                    } catch (e: Exception) {
                        0
                    }

                    if (missionData != null) {
                        try {
                            val autoSize = missionData!!.auto.size
                            if (missionIdx >= autoSize) {
                                missionIdx = autoSize - 1
                            }

                            loadUrl = missionData!!.auto[missionIdx].landing!!
                            timeRemaining = missionData!!.auto[missionIdx].timer!!.toLong() * 1000

                            setPoMissionTopLayout()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                ViewType.NEWS.name -> {
                    newsPoint = try {
                        it.getIntExtra("newsPoint", 1)
                    } catch (e: Exception) {
                        1
                    }

                    newsThresholdSec = try {
                        it.getIntExtra("thresholdSec", 10)
                    } catch (e: Exception) {
                        10
                    }

                    isAvailable = try {
                        it.getBooleanExtra("isAvailable", false)
                    } catch (e: Exception) {
                        false
                    }

                    guid = try {
                        it.getStringExtra("guid")
                    } catch (e: Exception) {
                        ""
                    }

                    var isDailyLimit = DAILY_NEWS_REWARD_LIMIT > PrefRepository.LockQuickInfo.dailyNewsfeedRewardCount
                    var isHourlyLimit = HOURLY_NEWS_REWARD_LIMIT > PrefRepository.LockQuickInfo.hourlyNewsfeedRewardCount

                    if (isAvailable && isDailyLimit && isHourlyLimit) {
                        timeRemaining = newsThresholdSec.toLong() * 1000
                        isNewsReward = true
                    }

                    setNewsfeedTopLayout(isNewsReward)
                }

                ViewType.LIVE_STREAMING.name -> {

                    if (loadUrl.isNotEmpty()) {
                        Uri.parse(loadUrl).getQueryParameter("sc")?.let { sc ->
                            PrefRepository.UserInfo.sc = sc
                        }
                    }

                    campaignInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        it.getParcelableExtra("campaignInfo", LockScreenResponse.CampaignInfo::class.java)
                    } else {
                        @Suppress("Deprecation")
                        it.getParcelableExtra("campaignInfo") as? LockScreenResponse.CampaignInfo
                    }

                    campaignData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        it.getParcelableExtra("campaignData", AdNonSDKMobileBannerResponse.Client::class.java)
                    } else {
                        @Suppress("Deprecation")
                        it.getParcelableExtra("campaignData") as? AdNonSDKMobileBannerResponse.Client
                    }

                    campaignInfo?.let { info ->
                        timeRemaining = info.thresholdSec.toLong() * 1000
                        setMobonTopLayout()
                    }
                }

                else -> {}
            }

            logScreenEvent(it)
        }
    }

    private fun logScreenEvent(intent: Intent) {
        val screenName = when (viewType) {
            ViewType.NORMAL.name -> {
                intent.getStringExtra(FirebaseAnalyticsManager.VIEW_NAME) ?: ""
            }

            ViewType.MISSION.name -> {
                "포미션 자동 미션"
            }

            ViewType.NEWS.name -> {
                if (isNewsReward) "뉴스 적립" else "뉴스"
            }

            ViewType.SHOP_PLUS.name -> {
                "샵플러스 쇼핑적립"
            }

            ViewType.LIVE_STREAMING.name -> {
                "모비온 라이브 캠페인"
            }

            else -> {
                ""
            }
        }

        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, screenName)
            putString(FirebaseAnalyticsManager.START_POINT, "lockScreen")
        })

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!PrefRepository.SettingInfo.useLockScreen) {
            finish()
        }

        isTimerFinish = false
    }

    override fun onDestroy() {
        super.onDestroy()

        pauseTimer()

        if (dialog != null) {
            dialog = null
        }
    }

    private fun setupWebViewWithTune720(webView: WebView?, hostUrl: String?) {
        try {
            if (ENDataManager.isInitialized()) {
                ENDataManager.getInstance().setWebView(webView, hostUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onWebViewPageStartedWithTune720(webView: WebView?, hostUrl: String?) {
        try {
            if (ENDataManager.isInitialized()) {
                ENDataManager.getInstance().onWebViewPageStarted(webView, hostUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onWebViewPageFinishedWithTune720(webView: WebView?, hostUrl: String?) {
        try {
            if (ENDataManager.isInitialized()) {
                ENDataManager.getInstance().onWebViewPageFinished(webView, hostUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 원태크 스크립트 삽입
        webView?.evaluateJavascript(
            """
                (function() {
                    var script = document.createElement('script');
                    script.src = "https://cdn.onetag.co.kr/0/tcs.js?eid=soknezhqzqyfsoknezhqzq";
                    script.async = true;
                    document.head.appendChild(script);
                })();
            """.trimIndent(), null
        )
    }

    private fun observeViewModel() {
        viewModel.isPoMissionAuto.observe(this) { isMissionClear ->
            isMissionClear?.let {
                missionIdx += 1

                var intent = Intent()
                intent.putExtra("type", ViewType.MISSION.name)
                intent.putExtra("isMissionClear", isMissionClear)
                intent.putExtra("missionIdx", missionIdx)
                setResult(RESULT_OK, intent)

                this@LockScreenWebViewActivity.finish()
            }
        }

        viewModel.isMobonReward.observe(this) { isMobonReward ->
            isMobonReward?.let {
                if (isMobonReward) {
                    val msg = "${campaignInfo?.point ?: 1}P 적립 완료되었습니다"
                    CustomToast.showToast(this, msg)
                }
            }
        }

        viewModel.isNewsReward.observe(this) { isNewsReward ->
            isNewsReward?.let {
                if (isNewsReward) {
                    PrefRepository.LockQuickInfo.dailyNewsfeedRewardCount += 1
                    PrefRepository.LockQuickInfo.hourlyNewsfeedRewardCount += 1
                }
            }
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (viewType) {
                    ViewType.MISSION.name -> {
                        // 미션 실행시 팝업 설정으로 인해 뒤로가기 사용안함
                        val tempTime = System.currentTimeMillis()
                        val intervalTime = tempTime - backPressedTime
                        if (intervalTime in 0..FINISH_INTERVAL_TIME) {
                            this@LockScreenWebViewActivity.finish()
                        } else {
                            backPressedTime = tempTime
                            CustomToast.showToast(this@LockScreenWebViewActivity, getString(R.string.toast_message_back_pressed))
                        }
                    }

                    ViewType.LIVE_STREAMING.name -> {
                        if (isTimerFinish) {
                            var intent = Intent()
                            intent.putExtra("type", ViewType.LIVE_STREAMING.name)
                            setResult(RESULT_OK, intent)

                            this@LockScreenWebViewActivity.finish()
                        }
                    }

                    ViewType.NEWS.name -> {
                        pauseTimer()

                        if (isNewsReward && isTimerFinish) {
                            var intent = Intent()
                            intent.putExtra("type", ViewType.NEWS.name)
                            intent.putExtra("newsPoint", newsPoint)
                            try {
                                viewModel.isNewsReward.value?.let {
                                    if (it) {
                                        intent.putExtra("watchedNews", guid ?: "")
                                    } else {
                                        intent.putExtra("watchedNews", "")
                                    }
                                }
                            } catch (e: Exception) {
                                intent.putExtra("watchedNews", "")
                            }
                            setResult(RESULT_OK, intent)
                        }
                        this@LockScreenWebViewActivity.finish()
                    }

                    else -> {
                        if (viewDataBinding.webView.canGoBack()) {
                            viewDataBinding.webView.goBack()
                        } else {
                            this@LockScreenWebViewActivity.finish()
                        }
                    }
                }
            }
        }

    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    private fun setWebView() {
        viewDataBinding.webView.apply {
            // WebView Debugging
//            setWebContentsDebuggingEnabled(true)

            setPadding(0, 0, 0, 0)

            with(settings) {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true
            }

            webViewClient = mWebViewClient
            setOnScrollChangeListener(scrollChangeListener)
            addJavascriptInterface(AndroidBridge(this@LockScreenWebViewActivity), BRIDGE_NAME)

            setupWebViewWithTune720(viewDataBinding.webView, loadUrl)
            loadUrl(loadUrl)
        }
    }

    private fun doPageStarted(view: WebView?) {
        if (missionData == null) return

        val auto = missionData?.auto ?: emptyList()
        val mission = missionData?.mission ?: emptyList()
        if (auto.size - 1 < missionIdx || mission.size - 1 < missionIdx) return

        Thread {
            try {
                val scriptUrl = auto[missionIdx].script
                val missionData: String = Gson().toJson(mission[missionIdx])
                val javascript = scriptUrl
                    ?.let { fetchJavaScript(it) }
                    ?.replace("{jsMission}", missionData)
                    ?: ""

                view?.post { view.evaluateJavascript(javascript, null) }
            } catch (e: Exception) {
                Logger.e(e.message)
            }
        }.start()
    }

    @Inject
    @Named("DynamicUrlOkHttpClient")
    lateinit var okHttpClient: OkHttpClient

    private fun fetchJavaScript(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()

        val response = okHttpClient.newCall(request).execute()

        return try {
            if (response.isSuccessful) {
                response.body?.string() ?: "No content"
            } else {
                "Failed=${response.code}"
            }
        } catch (e: IOException) {
            "IOException=${e.message}"
        } catch (e: Exception) {
            "Exception=${e.message}"
        }
    }

    private fun resultStr(str: String) = try {
        var firstIndex = 0
        var secondIndex = str.indexOf("\"")
        var lastIndex = str.indexOf("\"", secondIndex + 1)

        var firstStr = str.substring(firstIndex, secondIndex).replace("\"", "")
        var secondStr = str.substring(secondIndex + 1, lastIndex).replace("\"", "")
        var lastStr = str.substring(lastIndex + 1, str.length).replace("\"", "")

        if (secondIndex == 0) {
            firstStr = " "
            secondIndex = 1
            lastIndex += 1
        }

        val builder = SpannableStringBuilder(
            SpannableString(firstStr)
        ).append(secondStr).append(lastStr)

        builder.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this@LockScreenWebViewActivity, R.color.grey_222)),
            firstIndex,
            secondIndex - 1,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        builder.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this@LockScreenWebViewActivity, R.color.orange_color)),
            secondIndex,
            lastIndex - 1,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        builder.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this@LockScreenWebViewActivity, R.color.grey_222)),
            lastIndex,
            builder.length,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        builder
    } catch (e: Exception) {
        SpannableStringBuilder(str)
    }

    private fun startTimer() {
        // 기존 타이머 있으면 취소 (중복 방지)
        pauseTimer()

        // 타이머 시작 또는 이어서 진행
        countDownTimer = object : CountDownTimer(timeRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(minutes)

                when (viewType) {
                    ViewType.NORMAL.name -> {}

                    ViewType.MISSION.name -> {}

                    ViewType.NEWS.name -> {
//                        timeRemaining = millisUntilFinished
//
//                        showTimerText()
//
//                        if (seconds > 0) {
//                            setTimerText("$seconds${getString(R.string.news_point_message_1)}")
//                        } else {
//                            setTimerText(getString(R.string.news_point_message_3))
//                        }
                        viewDataBinding.countTV.text = seconds.toString()
                    }

                    ViewType.SHOP_PLUS.name -> {}

                    ViewType.LIVE_STREAMING.name -> {
                        viewDataBinding.countTV.text = seconds.toString()
                    }
                }
            }

            override fun onFinish() {
                when (viewType) {
                    ViewType.NORMAL.name -> {}

                    ViewType.MISSION.name -> {
                        if (!isFinishing && !isDestroyed && dialog != null) {
                            dialog?.dismissDialog()
                            dialog = null
                        }

                        if (!isTimerFinish && !isStopMission) {
                            viewModel.poMissionAuto(missionData!!.mission[missionIdx], "0")
                            isTimerFinish = true
                        }
                    }

                    ViewType.NEWS.name -> {
                        guid?.let { viewModel.newsReward(it, newsPoint) }

                        showCloseBtn()

                        isTimerFinish = true
                    }

                    ViewType.SHOP_PLUS.name -> {}

                    ViewType.LIVE_STREAMING.name -> {
                        viewModel.mobonReward()

                        showCloseBtn()

                        isTimerFinish = true
                    }
                }
            }
        }.start()
    }

    private fun pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer?.cancel()
        }
    }

//    private fun setTimerText(text: String) {
//        viewDataBinding.apply {
//            timerTV.text = text
//        }
//    }
//
//    private fun showTimerText() {
//        viewDataBinding.apply {
//            timerTV.visibility = View.VISIBLE
//        }
//    }
//
//    private fun hideTimerText() {
//        viewDataBinding.apply {
//            timerTV.visibility = View.GONE
//        }
//    }

    private fun showCloseBtn() {
        viewDataBinding.apply {
            countTV.visibility = View.GONE
            closeIV.visibility = View.VISIBLE
        }
    }

    private fun setPoMissionTopLayout() {
        viewDataBinding.apply {
            topLayout.visibility = View.VISIBLE
            closeBtn.visibility = View.GONE

            titleTV.text = if (missionData != null) {
                missionData?.mission!![missionIdx].adver_name
            } else {
                getString(R.string.pomission_title)
            }
        }
    }

    private fun setMobonTopLayout() {
        viewDataBinding.apply {
            topLayout.visibility = View.VISIBLE

            campaignData?.let { campaignData ->
                campaignData.data?.let { data ->
                    if (data.isNotEmpty()) {
                        titleTV.text = data[0].site_title ?: ""
                    }
                }
            }

            campaignInfo?.let { campaignInfo ->
                countTV.text = campaignInfo.thresholdSec.toString()
            }

            closeBtn.setOnClickListener {
                if (View.VISIBLE == closeIV.visibility) {
                    var intent = Intent()
                    intent.putExtra("type", ViewType.LIVE_STREAMING.name)
                    setResult(RESULT_OK, intent)
                    this@LockScreenWebViewActivity.finish()
                }
            }
        }
    }

    /**
     * @param isNewsReward
     */
    private fun setNewsfeedTopLayout(isNewsReward: Boolean) {
        viewDataBinding.apply {
            topLayout.visibility = View.VISIBLE

            if (isNewsReward) {
                titleTV.text = getString(R.string.news_reward)
            } else {
                titleTV.text = getString(R.string.news)
                showCloseBtn()
            }

            countTV.text = newsThresholdSec.toString()

            closeBtn.setOnClickListener {
                if (isNewsReward) {
                    var intent = Intent()
                    intent.putExtra("type", ViewType.NEWS.name)
                    intent.putExtra("newsPoint", newsPoint)
                    try {
                        viewModel.isNewsReward.value?.let {
                            if (it) {
                                intent.putExtra("watchedNews", guid ?: "")
                            } else {
                                intent.putExtra("watchedNews", "")
                            }
                        }
                    } catch (e: Exception) {
                        intent.putExtra("watchedNews", "")
                    }
                    setResult(RESULT_OK, intent)
                }
                this@LockScreenWebViewActivity.finish()
            }
        }
    }

    /**
     * 포미션 미션 수행 중단 팝업
     */
    private fun showStopMissionPopup() {
        HCCommonDialog(this@LockScreenWebViewActivity)
            .setDialogType(DialogType.ALERT)
            .setContent(R.string.popup_stop_mission_text)
            .setPositiveButtonText(R.string.popup_stop_mission_positive_button)
            .setNegativeButtonText(R.string.popup_stop_mission_negative_button)
            .setOnDismissListener(object : HCCommonDialog.OnDismissListener {
                override fun onDismiss(menuId: Int) {
                    when (menuId) {
                        DialogType.BUTTON_POSITIVE.ordinal -> {
                            isStopMission = true
                            pauseTimer()
                            this@LockScreenWebViewActivity.finish()
                        }
                    }
                }
            })
            .show()
    }

    inner class AndroidBridge(val context: Context) {

        @JavascriptInterface
        fun close() {
            this@LockScreenWebViewActivity.finish()
        }

        @JavascriptInterface
        fun startAutoRun(msg: String) {
            Logger.d("startAutoRun.. msg=${msg}")

            startTimer()

            if (dialog == null) {
                dialog = ProgressDialog(this@LockScreenWebViewActivity)

                dialog?.apply {
                    if (TextUtils.isEmpty(msg)) {
                        setText(getString(R.string.dialog_progress_default_message))
                    } else {
                        setText(resultStr(msg))
                    }

                    setOnClickListener(object : ProgressDialog.OnClickListener {
                        override fun onClose() {
                            showStopMissionPopup()
                        }
                    })

                    showDialog()
                }
            }
        }

        @JavascriptInterface
        fun arrivedBottom(step: String) {
            Logger.d("arrivedBottom.. step=$step")

            if (!isFinishing && !isDestroyed && dialog != null) {
                dialog?.dismissDialog()
                dialog = null
            }

            if (!isStopMission) {
                viewModel.poMissionAuto(missionData!!.mission[missionIdx], step)
            }
        }
    }
}