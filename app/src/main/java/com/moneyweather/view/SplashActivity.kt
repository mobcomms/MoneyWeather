package com.moneyweather.view

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.data.remote.response.AppVersionResponse
import com.moneyweather.databinding.ActivitySplashBinding
import com.moneyweather.event.intro.IntroUiEffect
import com.moneyweather.event.intro.IntroUiEvent
import com.moneyweather.extensions.checkUpdate
import com.moneyweather.listener.AppFinishListener
import com.moneyweather.model.enums.ActivityEnum
import com.moneyweather.model.enums.DialogType
import com.moneyweather.model.enums.SignUpType
import com.moneyweather.ui.dialog.HCCommonDialog
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.CommonUtils.Companion.OnCallbackAdidListener
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.Logger
import com.moneyweather.util.PermissionUtils
import com.moneyweather.util.PrefRepository
import com.moneyweather.viewmodel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.Serializable

/**
 * 앱 인트로 화면
 */
@AndroidEntryPoint
class SplashActivity : BaseKotlinActivity<ActivitySplashBinding, SplashViewModel>(),
    View.OnClickListener,
    AppFinishListener {

    override val layoutResourceId: Int get() = R.layout.activity_splash
    override val viewModel: SplashViewModel by viewModels()

    private var moveActivity: ActivityEnum? = null
    private var path: String? = null

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)

        getIntentData()
        observeViewModel()

        viewModel.dispatchEvent(IntroUiEvent.FetchAppVersion)
    }

    private fun startApp() {
        getInstallReferrer()
        getAdId()
        getFcmToken()
        checkPermission()
    }

    // 권한 체크
    private fun checkPermission() {
        if (checkLocationPermission() && checkOverlayPermission() && checkOverlayPermission()) {
            checkLogin()
        } else {
            popupPermissionInfo()
        }
    }

    // 권한 안내 팝업
    private fun popupPermissionInfo() {
        val dialog: HCCommonDialog = HCCommonDialog(this@SplashActivity)
            .setDialogType(DialogType.CONFIRM)
            .setLayout(R.layout.popup_permission_info)
            .setPositiveButtonText(R.string.confirm)
            .setOnDismissListener(object : HCCommonDialog.OnDismissListener {
                override fun onDismiss(menuId: Int) {
                    if (menuId == DialogType.BUTTON_CONFIRM.ordinal) {
                        if (checkLocationPermission()) {
                            requestOtherPermission()
                        } else {
                            requestLocationPermission.launch(REQUEST_LOCATION_PERMISSIONS)
                        }
                    }
                }
            })
        dialog.setCancelable(false)
        dialog.show()
    }

    // 위치 권한 ====================================================================================
    private fun checkLocationPermission(): Boolean {
        return PermissionUtils.isGrantedPermission(this@SplashActivity, REQUEST_LOCATION_PERMISSIONS)
    }

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        requestOtherPermission()
    }

    // 전화 및 알림 권한 =============================================================================
    private fun checkOtherPermission() = PermissionUtils.isGrantedPermission(
        this@SplashActivity, REQUEST_PERMISSIONS
    )

    private fun requestOtherPermission() {
        if (checkOtherPermission()) {
            requestOverlayPermission()
        } else {
            requestOtherPermission.launch(REQUEST_PERMISSIONS)
        }
    }

    private val requestOtherPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { requestOverlayPermission() }

    // 다른 앱 위에 표시 권한 =========================================================================
    private fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this@SplashActivity)
    }

    private fun requestOverlayPermission() {
        if (checkOverlayPermission()) {
            checkLogin()
        } else {
            popupOverlay()
        }
    }

    private fun popupOverlay() {
        val first_str = getString(R.string.popup_overlay_title1).plus(" ")
        val second_str = getString(R.string.popup_overlay_title2)
        val last_str = getString(R.string.popup_overlay_title3)

        val spannableString = SpannableString(first_str)
        val builder = SpannableStringBuilder(spannableString)
        builder.append(second_str)
        builder.append(last_str)
        val begin = 0
        val end = first_str.length
        builder.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this@SplashActivity, R.color.default_main_color)),
            begin,
            end,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        val begin2 = builder.length - last_str.length
        val end2 = builder.length
        builder.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this@SplashActivity, R.color.grey_74)),
            begin2,
            end2,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        val dialog: HCCommonDialog = HCCommonDialog(this@SplashActivity)
            .setDialogType(DialogType.CONFIRM)
            .setLayout(R.layout.popup_overlay)
            .setContent(builder)
            .setPositiveButtonText(R.string.confirm)
            .setOnDismissListener(object : HCCommonDialog.OnDismissListener {
                override fun onDismiss(menuId: Int) {
                    if (menuId == DialogType.BUTTON_CONFIRM.ordinal) {
                        val uri = Uri.fromParts("package", packageName, null)
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
                        requestOverlayPermission.launch(intent)
                    }
                }
            })
        dialog.setCancelable(false)
        dialog.show()
    }

    private val requestOverlayPermission = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { checkLogin() }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        getInstallReferrer()
    }

    override fun onFinish(dialog: Dialog) {
        TODO("Not yet implemented")
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is IntroUiEffect.CheckAppUpdate -> {
                            checkAppUpdate(effect.data)
                        }

                        is IntroUiEffect.CheckLogin -> {
                            checkLogin()
                        }
                    }
                }
            }
        }
    }

    private fun getIntentData() {
        intent?.let {
            moveActivity = it.intentSerializable("move_activity", ActivityEnum::class.java)

            when (it.action) {
                Intent.ACTION_VIEW -> {
                    it.data?.let { uri ->
                        path = uri.path
                    }
                    return@let
                }

                Intent.ACTION_SEND -> { // 공유 받은 데이터 처리
                    val sharedText = it.getStringExtra(Intent.EXTRA_TEXT)
                    if (sharedText != null) {
                        // TODO: ...
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    private fun getFcmToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            PrefRepository.UserInfo.fcmToken = it
        }
    }

    private fun getAdId() {
        CommonUtils.getAdId(this, object : OnCallbackAdidListener {
            override fun onCallbackADID(adid: String?) {
                if (adid != null) {
                    PrefRepository.UserInfo.adid = adid
                }
            }
        })
    }

    private fun checkAppUpdate(data: AppVersionResponse.Data?) {
        if (data == null) {
            startApp()
            return
        }

        val isUpdated = checkUpdate(data) {
            startApp()
        }

        if (!isUpdated) {
            startApp()
        }
    }

    private fun checkLogin() {
        if (PrefRepository.UserInfo.accessToken.isEmpty()) {
            PrefRepository.SettingInfo.clearUseLockScreen()
            guestLogin() // accessToken 가져옴
            return
        }

        if (PrefRepository.UserInfo.isLogin || PrefRepository.UserInfo.isGuestLogin) {
            when (PrefRepository.UserInfo.serviceType) {
                SignUpType.GOOGLE.type,
                SignUpType.KAKAO.type,
                SignUpType.NAVER.type -> socialLogin()

                SignUpType.SERVICE.type -> emailLogin()
                else -> refreshSession() // 게스트 로그인
            }
        } else {
            moveToLogin()
        }
    }

    private fun socialLogin() {
        viewModel.dispatchEvent(IntroUiEvent.RequestLogin(moveActivity, path))
    }

    private fun emailLogin() {
        val email = PrefRepository.UserInfo.saveEmail
        val password = PrefRepository.UserInfo.savePassword

        if (email.isEmpty() || password.isEmpty()) {
            viewModel.dispatchEvent(IntroUiEvent.MoveToLogin)
        } else {
            viewModel.dispatchEvent(IntroUiEvent.RequestLogin(moveActivity, path))
        }
    }

    private fun guestLogin() {
        viewModel.dispatchEvent(IntroUiEvent.RequestGuestLogin)
    }

    private fun refreshSession() {
        viewModel.dispatchEvent(IntroUiEvent.RefreshSession(moveActivity, path))
    }

    private fun moveToLogin() {
        viewModel.dispatchEvent(IntroUiEvent.MoveToLogin)
    }

    /**
     * 친구 초대로 앱 설치한 경우 추천인 코드 가져옴
     */
    private fun getInstallReferrer() {
        try {
            val referrerClient = InstallReferrerClient.newBuilder(this).build()
            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        try {
                            val response = referrerClient.installReferrer
                            val referrerUrl = response.installReferrer

                            val recommendCode = Uri.parse("https://play.google.com/store?$referrerUrl")
                                .getQueryParameter("recommand_code")

                            if (!recommendCode.isNullOrEmpty()) {
                                PrefRepository.UserInfo.inviteRecommendCode = recommendCode
                            }

                            referrerClient.endConnection()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Logger.e("Install Referrer API failed with response code: $responseCode")
                    }
                }

                override fun onInstallReferrerServiceDisconnected() {
                    Logger.e("Install Referrer service disconnected")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun <T : Serializable> Intent.intentSerializable(key: String, clazz: Class<T>): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.getSerializableExtra(key, clazz)
        } else {
            this.getSerializableExtra(key) as T?
        }
    }

    companion object {
        val REQUEST_LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val REQUEST_SERVICE_PERMISSION = when {
            Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU -> arrayOf(
                Manifest.permission.FOREGROUND_SERVICE_LOCATION,
                Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE
            )

            else -> emptyArray()
        }

        val REQUEST_PERMISSIONS = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_PHONE_NUMBERS,
                Manifest.permission.POST_NOTIFICATIONS
            )

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_PHONE_NUMBERS
            )

            else -> arrayOf(
                Manifest.permission.READ_PHONE_STATE
            )
        }
    }
}