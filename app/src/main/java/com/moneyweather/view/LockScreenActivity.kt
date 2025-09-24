package com.moneyweather.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.enliple.datamanagersdk.ENDataManager
import com.enliple.datamanagersdk.events.models.ENCustomEvent
import com.google.android.gms.location.ActivityTransition
import com.moneyweather.R
import com.moneyweather.adapter.FragmentAdapter
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.databinding.ActivityLockscreenBinding
import com.moneyweather.event.lockscreen.LockScreenUiEvent
import com.moneyweather.extensions.checkUpdate
import com.moneyweather.listener.AppFinishListener
import com.moneyweather.model.enums.DialogType
import com.moneyweather.model.enums.ThemeType
import com.moneyweather.ui.dialog.HCCommonDialog
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.EventBus
import com.moneyweather.util.PrefRepository
import com.moneyweather.util.analytics.GaLockScreenViewEvent
import com.moneyweather.view.fragment.ThemeBackGroundFragment
import com.moneyweather.view.fragment.ThemeCalendarFragment
import com.moneyweather.view.fragment.ThemeInfoFragment
import com.moneyweather.view.fragment.ThemeSimpleFragment
import com.moneyweather.view.fragment.ThemeVideoFragment
import com.moneyweather.viewmodel.LockScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date

@AndroidEntryPoint
class LockScreenActivity : BaseKotlinActivity<ActivityLockscreenBinding, LockScreenViewModel>(),
    View.OnClickListener,
    AppFinishListener {

    override val layoutResourceId: Int get() = R.layout.activity_lockscreen
    override val viewModel: LockScreenViewModel by viewModels()

    private var popupNewPointInfo: HCCommonDialog? = null

    private val timeTickReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent) {
            if (intent.action == Intent.ACTION_TIME_TICK) {
                EventBus.post(intent)
            }
        }
    }

    @SuppressLint("WrongConstant")
    private fun removeActivityAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                ActivityTransition.ACTIVITY_TRANSITION_ENTER,
                R.anim.none,
                R.anim.none
            )
        } else {
            overridePendingTransition(R.anim.none, R.anim.none)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initStartView() {
        // 테마 화면 별 적용을 위해 Activity에 적용된 padding 제거
        CommonUtils.removeSystemBarPadding(viewDataBinding.root)

        removeActivityAnimation()

        instance = this

        viewModel.dispatchReplayEvent(LockScreenUiEvent.AppUpdateCheck)
        observeViewModel()

        replaceThemeFragment(PrefRepository.SettingInfo.selectThemeType)

        tuneEventStartLockscreen()

        val filter = IntentFilter(Intent.ACTION_TIME_TICK)
        registerReceiver(timeTickReceiver, filter)
    }

    private fun observeViewModel() {
        viewModel.resultAppVersion.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { data -> checkUpdate(data) }
            .launchIn(lifecycleScope)
    }

    override fun onNewIntent(intent: Intent) {
        removeActivityAnimation()
        super.onNewIntent(intent)

        refreshLockScreen(fromCreated = false)

        // Ga Log Event
        GaLockScreenViewEvent.logLockScreenThemeViewEvent(PrefRepository.SettingInfo.selectThemeType)
    }

    /**
     * Activity 생성 시(onCreate), Activity 재 생성시(onNewIntent), 테마가 바뀐 뒤 호출하여 데이터 갱신
     */
    private fun refreshLockScreen(fromCreated: Boolean = true, isThemeChange: Boolean = false) {
        viewModel.dispatchReplayEvent(LockScreenUiEvent.RefreshData(fromCreated = fromCreated, isThemeChange = isThemeChange))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!PrefRepository.SettingInfo.useLockScreen)
            finish()

        if (PrefRepository.LockQuickInfo.isLockScreenFirst) {
            // 앱 설치 후 최초로 락스크린 화면을 켠 경우, 기존 포인트 정책 안내 팝업 띄움
            popupPointInfo()

            // New 포인트 안내 팝업은 띄우지 않음
            PrefRepository.SettingInfo.checkNewInfoPopupToday = CommonUtils.getCurrentDate()
        } else {
            if (PrefRepository.SettingInfo.checkNewInfoPopup) {
                // New 포인트 안내 팝업에 '다시 보지 않기' 버튼을 클릭한 상태면, 배터리 최적화 팝업 띄움
                checkIgnoreBatteryOptimizations()
            } else {
                // New 포인트 안내 팝업 노출 여부 확인 후 팝업 띄움 (하루 1회)
                val shouldShowContent = CommonUtils.shouldShowContent(
                    PrefRepository.SettingInfo.checkNewInfoPopupToday
                )
                if (shouldShowContent) {
                    popupNewPointInfo = popupNewPointInfo()
                }
            }
        }

        loadSoundPool(this)

        onBackPressedDispatcher.addCallback(this@LockScreenActivity, onBackPressedCallback)

        refreshLockScreen(fromCreated = true)

        // Ga Log Event
        GaLockScreenViewEvent.logLockScreenThemeViewEvent(PrefRepository.SettingInfo.selectThemeType)
    }

    /**
     * New 포인트 안내 팝업
     *  - 앱을 설치한 당일에는 팝업을 띄우지 않음
     *  - 00시 이후로 락스크린 화면을 켠 경우 (하루 1회)
     *  - '다시 보지 않기' 버튼을 클릭한 경우 팝업을 띄우지 않음
     *
     *  @return
     */
    private fun popupNewPointInfo(): HCCommonDialog {
        PrefRepository.SettingInfo.checkNewInfoPopupToday = CommonUtils.getCurrentDate()

        val dialog: HCCommonDialog = HCCommonDialog(this@LockScreenActivity)
            .setDialogType(DialogType.ALERT)
            .setLockPointPolicyWebViewLayout(
                url = resources.getString(R.string.lock_point_policy_info),
                height = resources.getDimensionPixelSize(R.dimen.lock_point_policy_info_height)
            )
            .setWeatherInfo(FragmentAdapter(supportFragmentManager, lifecycle))
            .setNegativeButtonText(getString(R.string.popup_new_info_left_button_text))
            .setPositiveButtonText(getString(R.string.popup_new_info_right_button_text))
            .setOnDismissListener(object : HCCommonDialog.OnDismissListener {
                override fun onDismiss(menuId: Int) {
                    when (menuId) {
                        DialogType.BUTTON_NEGATIVE.ordinal -> {
                            PrefRepository.SettingInfo.checkNewInfoPopup = true
                        }

                        DialogType.BUTTON_POSITIVE.ordinal -> {}
                    }
                }
            })
        dialog.show()
        return dialog
    }

    /**
     * 포인트 정책 안내 팝업
     *  - 앱을 설치하고 최초 락스크린 화면을 켠 경우 (최초 1회)
     */
    private fun popupPointInfo() {
        val dialog: HCCommonDialog = HCCommonDialog(this@LockScreenActivity)
            .setDialogType(DialogType.CONFIRM)
            .setMainPointPolicyWebViewLayout(
                url = resources.getString(R.string.main_point_policy_info),
                height = resources.getDimensionPixelSize(R.dimen.main_point_policy_info_height)
            )
            .setConfirmButtonText(R.string.confirm)
            .setOnDismissListener(object : HCCommonDialog.OnDismissListener {
                override fun onDismiss(menuId: Int) {
                    if (menuId == DialogType.BUTTON_CONFIRM.ordinal) {
                        checkIgnoreBatteryOptimizations()
                        PrefRepository.LockQuickInfo.isLockScreenFirst = false
                    }
                }
            })
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun checkIgnoreBatteryOptimizations() {
        // 하루 한번 배터리 최적화 OFF 요청
        val currentDate: Long = SimpleDateFormat("yyyyMMdd").format(Date()).toLong()
        val saveDate = PrefRepository.LockQuickInfo.checkBatteryOptimizations
        if (currentDate > saveDate) {
            requestIgnoreBatteryOptimizations(currentDate)
        }
    }

    private fun requestIgnoreBatteryOptimizations(currentDate: Long) {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val dialog: HCCommonDialog = HCCommonDialog(this@LockScreenActivity)
                .setDialogType(DialogType.CONFIRM)
                .setLayout(R.layout.popup_battery)
                .setPositiveButtonText(R.string.confirm)
                .setOnDismissListener(object : HCCommonDialog.OnDismissListener {
                    override fun onDismiss(menuId: Int) {
                        if (menuId == DialogType.BUTTON_CONFIRM.ordinal) {
                            val intent = Intent()
                            intent.setAction(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                            intent.setData(Uri.parse("package:$packageName"))
                            startActivity(intent)
                        }
                    }
                })
            dialog.show()
            PrefRepository.LockQuickInfo.checkBatteryOptimizations = currentDate
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        timeTickReceiver.let(::unregisterReceiver)

        releaseSoundPool()
    }

    override fun onFinish(dialog: Dialog) {

    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(0, 0)

        popupNewPointInfo?.let {
            if (!it.isShowing) {
                // New 포인트 안내 팝업이 먼저 뜨고 닫힌 경우, 배터리 최적화 팝업을 띄움 (하루 1회)
                checkIgnoreBatteryOptimizations()
            }
        }

        val isThemeChanged = PrefRepository.LockQuickInfo.isThemeChange
        refreshLockScreen(fromCreated = false, isThemeChange = isThemeChanged)

        if (isThemeChanged) {
            PrefRepository.LockQuickInfo.isThemeChange = false
            replaceThemeFragment(PrefRepository.SettingInfo.selectThemeType)

            // Ga Log Event
            GaLockScreenViewEvent.logLockScreenThemeViewEvent(PrefRepository.SettingInfo.selectThemeType)
        }
    }

    private fun replaceThemeFragment(selectThemeType: Int) {
        when (selectThemeType) {
            ThemeType.INFO.ordinal -> replaceFragment(
                R.id.fragmentContainer,
                ThemeInfoFragment::class.java,
                intent
            )

            ThemeType.CALENDAR.ordinal -> replaceFragment(
                R.id.fragmentContainer,
                ThemeCalendarFragment::class.java,
                intent
            )

            ThemeType.SIMPLE.ordinal -> replaceFragment(
                R.id.fragmentContainer,
                ThemeSimpleFragment::class.java,
                intent
            )

            ThemeType.BACKGROUND.ordinal -> replaceFragment(
                R.id.fragmentContainer,
                ThemeBackGroundFragment::class.java,
                intent
            )

            ThemeType.VIDEO.ordinal -> replaceFragment(
                R.id.fragmentContainer,
                ThemeVideoFragment::class.java,
                intent
            )
        }
    }

    /**
     * 락스크린 실행시 Tune720 이벤트 호출
     */
    private fun tuneEventStartLockscreen() {
        val startLockscreenEvent = ENCustomEvent("락스크린 실행")
        val theme = ThemeType.fromInt(PrefRepository.SettingInfo.selectThemeType).name
        startLockscreenEvent.addCustomData("theme", theme)
        ENDataManager.getInstance().addEvent(startLockscreenEvent)
    }

    enum class ButtonType {
        COUPANG_CPS,
        LIVE_STREAMING,
        MISSION,
        DONG_DONG,
        OFFERWALL,
        POINT,
    }

    companion object {
        var instance: LockScreenActivity? = null
        const val EXTRA_IS_LOCK_SCREEN: String = "extra_is_lock_screen"

        const val FETCH_REFRESH_DATA_DELAY = 500

        private lateinit var soundPool: SoundPool
        private var soundId: Int = 0

        fun playSound() {
            if (!PrefRepository.SettingInfo.useSavePointSound) return

            try {
                soundPool.play(soundId, 0.2f, 0.2f, 1, 0, 1.0f)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun loadSoundPool(context: Context) {
            // 1. AudioAttributes 설정
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA) // 미디어 재생용
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) // 효과음 용도
                .build()

            // 2. SoundPool 초기화
            soundPool = SoundPool.Builder()
                .setMaxStreams(10) // 동시에 재생할 수 있는 스트림의 최대 개수
                .setAudioAttributes(audioAttributes) // AudioAttributes 연결
                .build()

            // 3. 효과음 로드
            soundId = soundPool.load(context, R.raw.coin_sound_2, 1)
        }

        fun releaseSoundPool() {
            soundPool.release()
        }

        /**
         * @param view
         */
        val animation = TranslateAnimation(0f, 0f, 0f, -30f).apply {
            duration = 1500
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE

            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {

                }

                override fun onAnimationRepeat(animation: Animation?) {

                }
            })
        }

        /**
         * @param point
         * @return ex) + 1,000,000
         */
        fun pointFormat(point: Long) = try {
            val sb = StringBuilder()
            val preStr = "+ "
            val pattern = "#,###"

            sb.apply {
                append(preStr)
                append(DecimalFormat(pattern).format(point))
            }

            sb.toString()
        } catch (e: Exception) {
            point.toString()
        }

        /**
         * 기본 포인트 적립 API 당일 처음 호출시 Tune720 이벤트 호출
         * @param point
         */
        fun tuneEventPointSave(point: Int) {
            val shouldShowContent = CommonUtils.shouldShowContent(
                PrefRepository.LockQuickInfo.checkDefaultPointSave
            )

            if (shouldShowContent) {
                val pointSaveEvent = ENCustomEvent("기본적립 포인트")
                pointSaveEvent.addCustomData("point", point)
                ENDataManager.getInstance().addEvent(pointSaveEvent)

                PrefRepository.LockQuickInfo.checkDefaultPointSave = CommonUtils.getCurrentDate()
            }
        }
    }
}


