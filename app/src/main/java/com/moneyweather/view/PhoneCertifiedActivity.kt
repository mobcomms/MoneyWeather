package com.moneyweather.view

import UserCertificationVO
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.databinding.ActivityPhoneCertifiedBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.ui.dialog.HCCommonRoundBtnDialog
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.IntentUtils
import com.moneyweather.util.Logger
import com.moneyweather.util.PrefRepository
import com.moneyweather.viewmodel.PhoneCertifiedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhoneCertifiedActivity : BaseKotlinActivity<ActivityPhoneCertifiedBinding, PhoneCertifiedViewModel>() {

    override val layoutResourceId: Int get() = R.layout.activity_phone_certified
    override val viewModel: PhoneCertifiedViewModel by viewModels()

    private lateinit var userCertificationVO: UserCertificationVO
    private var isSignUpCall: Boolean = false

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "본인인증하기")
        })

        viewDataBinding.vm = viewModel

        intent?.let {
            isSignUpCall = it.getBooleanExtra("signUp", false)
        }

        initActionBar(viewDataBinding.iActionBar, R.string.phone_certified, ActionBarLeftButtonEnum.BACK_BUTTON)
        init()

        viewModel.resultAuth.observe(this, androidx.lifecycle.Observer {
            when (it) {
                true -> {
                    val intent = Intent()
                    intent.putExtra(DATA_USER_CERTIFICATION, true)
                    setResult(RESULT_SUCCESS, intent)
                    finish()
                }

                false -> {
                    val intent = Intent()
                    intent.putExtra(DATA_USER_CERTIFICATION, false)
                    setResult(RESULT_SUCCESS, intent)
                    finish()
                }
            }

        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun init() {
        activity = this@PhoneCertifiedActivity

        var url = PrefRepository.SettingInfo.verificationUrl
//        if(BuildConfig.DEBUG) {
//            url = resources.getString(R.string.phone_certified_url_test)
//        }

        viewDataBinding.apply {
            webView?.loadUrl(url)

            webView?.settings?.setRenderPriority(WebSettings.RenderPriority.HIGH)
            webView?.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    Logger.d("shouldOverrideUrlLoading url : $url")
                    val intent = IntentUtils.parse(url)
                    if (intent != null) {
                        if (IntentUtils.isIntent(url)) {
                            return if (IntentUtils.isInnerIntent(view.context, intent) || IntentUtils.isExistPackage(view.context, intent)) {
                                startActivity(intent)
                                true
                            } else {
                                IntentUtils.gotoMarket(view.context, intent)
                                true
                            }
                        } else if (IntentUtils.isMarket(url)) {
                            startActivity(intent)
                            return true
                        }
                    }

                    return super.shouldOverrideUrlLoading(view, url)
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    Logger.d("url : $url")
                }
            }
            webView?.webChromeClient = WebChromeClient()
            webView?.setNetworkAvailable(true)
            webView?.settings?.javaScriptEnabled = true
            webView?.addJavascriptInterface(AndroidBridge(), "android")
        }

    }

    override fun onDestroy() {
        try {

        } catch (e: Exception) {
            Logger.e(e.message)
        }
        super.onDestroy()
    }

    inner class AndroidBridge {
        @JavascriptInterface
        fun setMessage(msg: String?) {
            getMessage(msg)
        }
    }

    private fun getMessage(msg: String?) {
        Logger.d("AndroidBridge", msg)
        val element = JsonParser.parseString(msg)

        userCertificationVO = Gson().fromJson(element, UserCertificationVO::class.java)

        if (userCertificationVO.code == "B000") {

            // val authReq = AuthReq(userCertificationVO.name, userCertificationVO.telNo,userCertificationVO.ci)
            //  PreferencesUtil.getInstance().putString(Key.KEY_AUTH_PHONE, userCertificationVO.telNo)
            PrefRepository.UserInfo.phone = userCertificationVO.telNo
            PrefRepository.UserInfo.ci = userCertificationVO.ci
            PrefRepository.UserInfo.name = userCertificationVO.name
            PrefRepository.UserInfo.birthday = userCertificationVO.birthday

            if (isSignUpCall) {
                intent.putExtra(DATA_USER_CERTIFICATION, true)
                setResult(RESULT_SUCCESS, intent)
                finish()
            } else {
                var m = HashMap<String, Any?>()
                m["ci"] = userCertificationVO.ci
                m["phone"] = userCertificationVO.telNo
                viewModel.connectModifyAuth(m)
            }

        } else {
            intent.putExtra(DATA_USER_CERTIFICATION, false)
            setResult(RESULT_FAILURE, intent)
            finish()
        }

    }


    private fun showDuplicatedUserAuthDialog(msg: String, email: String) {
        val depositorDialog = HCCommonRoundBtnDialog(this)
            .setDialogTitle(R.string.phone_certified)
            .setContent(msg, email)
            .setConfirmButtonText(getString(R.string.confirm))
            .setOnDismissListener(object : HCCommonRoundBtnDialog.OnDismissListener {
                override fun onDismiss(menuId: Int) {
                    setResult(RESULT_FAILURE)
                    finish()
                }
            })
        depositorDialog.show()
    }

    private fun showCommonAuthDialog(msg: String) {
        val depositorDialog = HCCommonRoundBtnDialog(this)
            .setDialogTitle(R.string.phone_certified)
            .setContent(msg)
            .setConfirmButtonText(getString(R.string.confirm))
            .setOnDismissListener(object : HCCommonRoundBtnDialog.OnDismissListener {
                override fun onDismiss(menuId: Int) {
                    setResult(RESULT_FAILURE)
                    finish()
                }
            })
        depositorDialog.show()
    }

    companion object {
        const val DATA_USER_CERTIFICATION = "DATA_USER_CERTIFICATION"
        const val RESULT_SUCCESS = 1
        const val RESULT_FAILURE = -1
        private var activity: Activity? = null
    }
}
