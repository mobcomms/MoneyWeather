package com.moneyweather.service

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat.TRANSLUCENT
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import android.view.WindowManager.LayoutParams.TYPE_PHONE
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import com.moneyweather.R
import com.moneyweather.data.remote.UrlHelper
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.data.remote.request.MobOnDongDongRewardRequest
import com.moneyweather.data.remote.response.LockScreenResponse
import com.moneyweather.databinding.OverlayProgressBinding
import com.moneyweather.extensions.toPx
import com.moneyweather.util.CustomToast
import com.moneyweather.view.LockScreenWebViewActivity.Companion.KEY_DONG_DONG
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class OverlayProgressService : Service() {

    @Inject
    lateinit var apiUserModel: ApiUserModel

    private val binding by lazy { OverlayProgressBinding.inflate(LayoutInflater.from(this)) }
    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private val keyguardManager by lazy { getSystemService(KEYGUARD_SERVICE) as KeyguardManager }
    private val checkForegroundHandler by lazy { Handler(Looper.getMainLooper()) }

    private val durationQueue: ArrayDeque<Pair<Int, Long>> = ArrayDeque()
    private var animator: ObjectAnimator? = null
    private var coinAnimation: AnimatorSet? = null
    private var dongDongInfo: LockScreenResponse.MobonDongDongInfo? = null
    private var isCancelled: Boolean = false

    private val compositeDisposable = CompositeDisposable()

    private val checkForegroundRunnable = object : Runnable {
        override fun run() {
            if (!isBrowserForeground()) {
                stopSelf()
                return
            }
            checkForegroundHandler.postDelayed(this, 500)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || intent.action == null) {
            return START_STICKY
        }

        when (intent.action) {
            ACTION_START_OVERLAY -> {
                getIntentData(intent)
                checkAndStartProgress()
            }

            ACTION_STOP_OVERLAY -> {
                stopSelf()
            }

            else -> {
                Timber.tag(TAG).d("Unknown action: ${intent.action}")
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        stopProgress()
    }

    private fun getIntentData(intent: Intent) {
        dongDongInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(KEY_DONG_DONG, LockScreenResponse.MobonDongDongInfo::class.java)
        } else {
            @Suppress("Deprecation")
            intent.getParcelableExtra(KEY_DONG_DONG) as? LockScreenResponse.MobonDongDongInfo
        }
    }

    private suspend fun unlockWaiting(timeoutMs: Long) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            delay(500)
            if (!keyguardManager.isKeyguardLocked) {
                return
            }
        }
    }

    private fun getExecutedAppPackageName(): String {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(UrlHelper.MOBON_DOMAIN))
        val resolveInfo = intent.resolveActivity(packageManager)
        return resolveInfo.packageName
    }

    private fun getForegroundAppPackageName(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 600_000L

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            beginTime,
            endTime
        )

        val recentUsage = usageStatsList
            ?.filter { it.packageName != "android" && it.totalTimeInForeground > 0 }
            ?.maxByOrNull { it.lastTimeUsed }

        return recentUsage?.packageName
    }

    private fun isBrowserForeground(): Boolean {
        return getExecutedAppPackageName() == getForegroundAppPackageName()
    }

    private fun startForegroundCheck() {
        checkForegroundHandler.postDelayed(checkForegroundRunnable, 500)
    }

    private fun checkAndStartProgress() {
        CoroutineScope(Dispatchers.Main).launch {
            unlockWaiting(timeoutMs = PARTICIPATION_POSSIBLE_TIME)

            if (keyguardManager.isKeyguardLocked) {
                stopSelf()
            } else {
                sequentialProgress()
            }
        }
    }

    private fun sequentialProgress() {
        isCancelled = false

        dongDongInfo?.let { info ->
            val durations = info.mobonDongDongConfigInfoList.mapIndexed { idx, config ->
                idx to config.paymentTime * 1_000L
            }

            durationQueue.apply {
                clear()
                addAll(durations)
            }
        }

        initViews()

        startForegroundCheck()
        startProgress()
    }

    private fun startProgress() {
        if (durationQueue.isEmpty()) {
            return
        }

        val (index, mDuration) = durationQueue.removeFirst()

        val max = (mDuration / 100).toInt()
        val progressBar = binding.progressBar.apply {
            this.max = max
            this.progress = 0
        }

        animator = ObjectAnimator.ofInt(progressBar, "progress", 0, max).apply {
            duration = mDuration
            interpolator = LinearInterpolator()

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)

                    if (index == 0) {
                        dongDongInfo?.let { info ->
                            requestRewardAPI(
                                dailyIntervalConfigId = info.mobonDongdongDailyIntervalConfigId,
                                configId = info.mobonDongDongConfigInfoList[index].mobonDongdongConfigId,
                                index = index,
                                point = info.mobonDongDongConfigInfoList[index].paymentPoint
                            )
                        }
                    }
                }

                override fun onAnimationEnd(animation: Animator) {
                    startCoinAnimation(index)

                    if (isCancelled) {
                        return
                    }

                    dongDongInfo?.let { info ->
                        if (index == 0) {
                            val msg = String.format(getString(R.string.toast_bottom_banner_point_message), info.mobonDongDongConfigInfoList[index].paymentPoint)
                            CustomToast.showToast(this@OverlayProgressService, msg)
                        } else if (index > 0) {
                            requestRewardAPI(
                                dailyIntervalConfigId = info.mobonDongdongDailyIntervalConfigId,
                                configId = info.mobonDongDongConfigInfoList[index].mobonDongdongConfigId,
                                index = index,
                                point = info.mobonDongDongConfigInfoList[index].paymentPoint
                            )
                        }
                    }

                    startProgress()
                }
            })
        }

        progressBar.post {
            animator?.start()
        }
    }

    private fun stopProgress() {
        hidePointProgress()

        dongDongInfo = null
        isCancelled = true
        animator?.cancel()
        coinAnimation?.cancel()
        durationQueue.clear()

        checkForegroundHandler.removeCallbacks(checkForegroundRunnable)

        if (binding.root.isAttachedToWindow) {
            windowManager.removeViewImmediate(binding.root)
        }
    }

    private fun initViews() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            105.toPx(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TYPE_APPLICATION_OVERLAY
            } else {
                TYPE_PHONE
            },
            FLAG_NOT_FOCUSABLE or FLAG_LAYOUT_NO_LIMITS,
            TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 32.toPx()
        }

        windowManager.addView(binding.root, params)

        showPointProgress()
        setPointText(dongDongInfo?.let { it.mobonDongDongConfigInfoList[0].paymentPoint } ?: 1)
    }

    private fun showPointProgress() {
        binding.clPointProgress.post {
            binding.clPointProgress.visibility = View.VISIBLE
        }
    }

    private fun hidePointProgress() {
        binding.clPointProgress.post {
            binding.clPointProgress.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setPointText(point: Int) {
        binding.tvPoint.apply {
            text = "${point}P"
        }
    }

    private fun startCoinAnimation(currentIndex: Int) {
        // 1. Flip 애니메이션 (Y축 회전)
        val flipAnimator = ObjectAnimator.ofFloat(binding.flCoin, "rotationY", 0f, 180f, 360f).apply {
            duration = COIN_ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = 0
        }

        // 2. 위로 올라가는 애니메이션
        val translateAnimator = ObjectAnimator.ofFloat(binding.flCoin, "translationY", 0f, -40.toPx().toFloat()).apply {
            duration = COIN_ANIMATION_DURATION
            interpolator = AccelerateInterpolator()
        }

        coinAnimation = AnimatorSet().apply {
            playTogether(flipAnimator, translateAnimator)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    dongDongInfo?.let { info ->
                        val lastIndex = info.mobonDongDongConfigInfoList.lastIndex
                        if (lastIndex > currentIndex) {
                            with(binding.flCoin) {
                                rotationY = 0f
                                translationY = 0f
                            }

                            val point = info.mobonDongDongConfigInfoList[currentIndex + 1].paymentPoint
                            setPointText(point)
                        } else if (info.mobonDongDongConfigInfoList.lastIndex == currentIndex) {
                            hidePointProgress()
                            stopSelf()
                        }
                    }
                }
            })

            start()
        }
    }

    private fun requestRewardAPI(
        dailyIntervalConfigId: Int,
        configId: Int,
        index: Int,
        point: Int
    ) {
        val param = MobOnDongDongRewardRequest(
            dailyIntervalConfigId,
            configId,
            if (index == 0) "Y" else "N"
        )

        val disposable = apiUserModel.mobOnDongDongReward(param)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (index > 0) {
                    val msg = String.format(getString(R.string.toast_bottom_banner_point_message), point)
                    CustomToast.showToast(this, msg)
                }
            }, { error ->
                error.message?.let { msg ->
                    Timber.tag(TAG).e(msg)
                }
            })

        compositeDisposable.add(disposable)
    }

    companion object {
        private const val PARTICIPATION_POSSIBLE_TIME: Long = 60_000L
        private const val COIN_ANIMATION_DURATION = 700L
        const val ACTION_START_OVERLAY = "actionStartOverlay"
        const val ACTION_STOP_OVERLAY = "actionStopOverlay"

        private val TAG: String = OverlayProgressService::class.java.simpleName
    }
}