package com.moneyweather.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.webkit.WebSettings
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.buzzvil.buzzbenefit.benefithub.BuzzBenefitHub
import com.buzzvil.sdk.BuzzvilSdk
import com.buzzvil.sdk.BuzzvilSdkLoginListener
import com.buzzvil.sdk.BuzzvilSdkUser
import com.igaworks.adpopcorn.Adpopcorn
import com.moneyweather.BuildConfig
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.data.remote.UrlHelper.MOBWITH_DOMAIN
import com.moneyweather.databinding.FragmentChargeBinding
import com.moneyweather.event.charge.ChargeUiEffect
import com.moneyweather.event.charge.ChargeUiEvent
import com.moneyweather.extensions.openOfferwall
import com.moneyweather.fcm.listener.setOnSingleClickListener
import com.moneyweather.model.enums.ActionBarRightButtonEnum
import com.moneyweather.model.enums.DialogType
import com.moneyweather.ui.dialog.HCCommonDialog
import com.moneyweather.util.CustomToast
import com.moneyweather.util.PrefRepository
import com.moneyweather.util.analytics.GaBannerClickEvent
import com.moneyweather.util.encryption.Aes256Crypto
import com.moneyweather.util.webview.BannerWebViewClient
import com.moneyweather.view.AppWebViewActivity
import com.moneyweather.view.LockScreenActivity
import com.moneyweather.view.LockScreenWebViewActivity
import com.moneyweather.view.LockScreenWebViewActivity.Companion.KEY_LOAD_URL
import com.moneyweather.view.LockScreenWebViewActivity.Companion.KEY_VIEW_TYPE
import com.moneyweather.view.MainActivity.Companion.getPincruxOfferwall
import com.moneyweather.view.MainActivity.Companion.getTnkOfferwall
import com.moneyweather.view.PhoneCertifiedActivity
import com.moneyweather.viewmodel.ChargeViewModel
import com.nextapps.naswall.NASWall
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ChargeFragment : BaseKotlinFragment<FragmentChargeBinding, ChargeViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_charge
    override val viewModel: ChargeViewModel by viewModels()

    override fun initStartView() {
        viewDataBinding.vm = viewModel

        initViews()
        observeEvent()

        viewModel.getMobwithScriptBanner()
    }

    private fun initViews() {
        initActionBar(viewDataBinding.iActionBar, R.string.bottom_nav_charge, ActionBarRightButtonEnum.NONE)

        viewDataBinding.apply {
            // Buzzvil
            ivBuzzvilButton.setOnSingleClickListener(1000) {
                if (PrefRepository.UserInfo.isLogin) {
                    viewModel.dispatchEvent(ChargeUiEvent.CheckVerification)

                    // Ga Log Event
                    GaBannerClickEvent.logOfferwallBannerBuzzvilClickEvent()
                } else {
                    CustomToast.showToast(requireContext(), R.string.message_non_login_user)
                }
            }

            // Pincrux
            ivPincruxButton.setOnSingleClickListener {
                if (BuildConfig.DEBUG) {
                    CustomToast.showToast(requireContext(), R.string.message_on_debug_mode)
                    return@setOnSingleClickListener
                }

                getPincruxOfferwall().startPincruxOfferwallActivity(requireContext())

                // Ga Log Event
                GaBannerClickEvent.logOfferwallBannerPincruxClickEvent()
            }

            // Pomission zone
            ivPomissionZoneButton.setOnSingleClickListener {
                if (BuildConfig.DEBUG) {
                    CustomToast.showToast(requireContext(), R.string.message_on_debug_mode)
                    return@setOnSingleClickListener
                }

                val pomissionZoneUrl = PrefRepository.LockQuickInfo.pomissionZoneUrl
                if (pomissionZoneUrl.isEmpty()) return@setOnSingleClickListener

                // pomission zone을 웹뷰로 노출
                val pomissionZoneIntent = Intent(requireActivity(), AppWebViewActivity::class.java).apply {
                    putExtra(KEY_LOAD_URL, pomissionZoneUrl)
                    putExtra(KEY_VIEW_TYPE, LockScreenWebViewActivity.ViewType.POMISSION_ZONE.name)
                    putExtra(LockScreenActivity.EXTRA_IS_LOCK_SCREEN, false)
                }
                startActivity(pomissionZoneIntent)

                // Ga Log Event
                GaBannerClickEvent.logOfferwallBannerPomissionClickEvent()
            }

            // Tnk
            ivTnkButton.setOnSingleClickListener {
                getTnkOfferwall(requireContext(), PrefRepository.UserInfo.userId)
                    .openOfferwall(requireContext())

                // Ga Log Event
                GaBannerClickEvent.logOfferwallBannerTnkClickEvent()
            }

            // AdPopCorn
            ivAdpopcornButton.setOnSingleClickListener {
                Adpopcorn.openOfferwall(requireContext())

                // Ga Log Event
                GaBannerClickEvent.logOfferwallBannerAdpopcornClickEvent()
            }

            // NasMedia
            ivNasButton.setOnSingleClickListener {
                checkAndRequestPermission()

                // Ga Log Event
                GaBannerClickEvent.logOfferwallBannerNasmediaClickEvent()
            }

        }
    }

    private fun observeEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect {
                    when (it) {
                        is ChargeUiEffect.ShowVerification -> {
                            verificationPopup()
                        }

                        is ChargeUiEffect.ShowBuzzvilOfferWall -> {
                            showBuzzBenefitHub()
                        }

                        is ChargeUiEffect.ShowToast -> {
                            CustomToast.showToast(requireContext(), it.message)
                        }
                    }
                }
            }
        }

        viewModel.bannerScript.observe(this, Observer { script ->
            script?.let {
                showBottomAdBanner(script)
            }
        })

        viewModel.isAvailableOfferWallAd.observe(this) { isAvailable ->
            isAvailable?.let {
                if (isAvailable) {
                    viewDataBinding.point.visibility = View.VISIBLE

                    val layoutParams = viewDataBinding.webView.layoutParams as FrameLayout.LayoutParams
                    viewDataBinding.webView.layoutParams = layoutParams.apply {
                        gravity = Gravity.START
                    }
                } else {
                    viewDataBinding.point.visibility = View.GONE

                    val layoutParams = viewDataBinding.webView.layoutParams as FrameLayout.LayoutParams
                    viewDataBinding.webView.layoutParams = layoutParams.apply {
                        gravity = Gravity.CENTER_HORIZONTAL
                    }
                }
            }
        }
    }

    private fun verificationPopup() {
        val dialog = HCCommonDialog(requireContext())
            .setDialogType(DialogType.ALERT)
            .setDialogTitle(R.string.certified)
            .setContent(R.string.certified_msg)
            .setPositiveButtonText(R.string.phone_certified2)
            .setOnDismissListener(object : HCCommonDialog.OnDismissListener {
                override fun onDismiss(menuId: Int) {
                    if (menuId == DialogType.BUTTON_POSITIVE.ordinal) {
                        startActivityForResult(PhoneCertifiedActivity::class.java, RESULT_CODE)
                    }
                }
            })
        dialog.show()

    }

    private fun checkPermission() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.GET_ACCOUNTS
    ) == PackageManager.PERMISSION_GRANTED

    private fun checkAndRequestPermission() {
        when {
            checkPermission() -> {
                // 권한이 이미 있는 경우
                initNasSDK()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.GET_ACCOUNTS) -> {
                // 유저가 이전에 권한 요청을 거부한 경우
                showCustomPermissionDialog()
            }

            else -> {
                // 권한 요청
                requestPermissionLauncher.launch(Manifest.permission.GET_ACCOUNTS)
            }
        }
    }

    private fun showCustomPermissionDialog() {
        try {
            val dialog: HCCommonDialog = HCCommonDialog(requireContext())
                .setDialogType(DialogType.ALERT)
                .setContent(R.string.dialog_message_request_nas_permission)
                .setPositiveButtonText(R.string.setting)
                .setNegativeButtonText(R.string.cancel)
                .setOnDismissListener(object : HCCommonDialog.OnDismissListener {
                    override fun onDismiss(menuId: Int) {
                        when (menuId) {
                            DialogType.BUTTON_POSITIVE.ordinal -> openAppSettings()
                            DialogType.BUTTON_NEGATIVE.ordinal -> {}
                        }
                    }
                })
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireActivity().packageName, null)
            }
            activityResultLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initNasSDK() {
        try {
            NASWall.init(activity, IS_TEST_MODE)
            NASWall.setOnInitListener(onNASWallInit)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openNasWall() {
        try {
            NASWall.open(requireActivity(), PrefRepository.UserInfo.userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showBuzzBenefitHub() {
        val encryptedUserId = Aes256Crypto.encrypt(PrefRepository.UserInfo.userId)

        if (BuzzvilSdk.isLoggedIn) {
            BuzzvilSdk.logout()
        }

        BuzzvilSdk.login(
            buzzvilSdkUser = BuzzvilSdkUser(userId = encryptedUserId),
            listener = object : BuzzvilSdkLoginListener {
                override fun onFailure(errorType: BuzzvilSdkLoginListener.ErrorType) {
                    Timber.tag("Buzzvil").d("login error : $errorType")
                }

                override fun onSuccess() {
                    Timber.tag("Buzzvil").d("login success")
                    BuzzBenefitHub.show(requireContext())
                }
            }
        )
    }

    /**
     * 모비위드 하단 띠배너
     * @param script
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun showBottomAdBanner(script: String) {
        with(viewDataBinding.webView) {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadsImagesAutomatically = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                allowFileAccess = true
                allowContentAccess = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36"
            }

            webViewClient = BannerWebViewClient(
                onUrlLoading = { _, _ ->
                    if (viewDataBinding.point.visibility == View.VISIBLE) {
                        viewModel.saveDailyAdPoint()
                    }
                },
                onFinished = { _, _ ->
                    viewModel.checkDailyAdPoint()
                }
            )

            loadDataWithBaseURL(
                MOBWITH_DOMAIN,
                script,
                "text/html",
                "utf-8",
                null
            )
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한 허용
            initNasSDK()
        } else {
            // 권한 거부
            showCustomPermissionDialog()
        }
    }

    private var activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (checkPermission()) {
            initNasSDK()
        }
    }

    private val onNASWallInit = NASWall.OnInitListener { openNasWall() }

    companion object {
        private const val IS_TEST_MODE = false
        private val RESULT_CODE: Int = 1122
    }
}