package com.moneyweather.view

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.enliple.datamanagersdk.ENDataManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.databinding.ActivityWebviewBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.util.CustomToast
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.Logger
import com.moneyweather.util.PermissionUtils
import com.moneyweather.util.webview.WebViewUtils
import com.moneyweather.view.LockScreenWebViewActivity.Companion.BRIDGE_NAME
import com.moneyweather.view.LockScreenWebViewActivity.Companion.KEY_LOAD_URL
import com.moneyweather.view.LockScreenWebViewActivity.Companion.KEY_VIEW_TYPE
import com.moneyweather.view.LockScreenWebViewActivity.ViewType
import com.moneyweather.viewmodel.WebviewViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AppWebViewActivity : BaseKotlinActivity<ActivityWebviewBinding, WebviewViewModel>() {

    override val layoutResourceId: Int get() = R.layout.activity_webview
    override val viewModel: WebviewViewModel by viewModels()

    private lateinit var loadUrl: String
    private lateinit var viewType: String

    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var downloadUrl: String = ""

    companion object {
        private const val BASE_URL = "https://weather.naver.com/"
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewDataBinding.webView.canGoBack()) {
                    viewDataBinding.webView.goBack()
                } else {
                    this@AppWebViewActivity.finish()
                }
            }
        }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (mFilePathCallback == null) return@registerForActivityResult

        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val uris: Array<Uri>? = WebChromeClient.FileChooserParams.parseResult(result.resultCode, intent)

            mFilePathCallback?.onReceiveValue(uris)
        } else {
            mFilePathCallback?.onReceiveValue(null)
        }

        mFilePathCallback = null
    }

    private val pomissionZoneWebChromeClient = object : WebChromeClient() {
        override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
            if (mFilePathCallback != null) {
                mFilePathCallback?.onReceiveValue(null)
                mFilePathCallback = null
            }

            mFilePathCallback = filePathCallback

            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                    setType("image/*")
                }
            } else {
                Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    setType("*/*") //모든 contentType 파일 표시
                }
            }

            pickImageLauncher.launch(intent)

            return true
        }
    }

    private val pomissionZoneWebViewClient = object : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            onWebViewPageStartedWithTune720(view, url)
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            onWebViewPageFinishedWithTune720(view, url)
            super.onPageFinished(view, url)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url?.toString() ?: return false

            try {
                when {
                    // 1. OAuth 콜백 처리
                    (url.startsWith("http://") || url.startsWith("https://")) &&
                            (url.contains("Oauth2ClientCallback/kakao") || url.contains("Oauth2ClientCallback/naver")) -> {
                        view?.loadUrl(url)
                        return true
                    }

                    // 2. 이미지 파일 다운로드
                    url.endsWith(".png") || url.endsWith(".jpg") -> {
                        if (PermissionUtils.hasStoragePermission(this@AppWebViewActivity)) {
                            downloadImage(url)
                        } else {
                            downloadUrl = url
                            PermissionUtils.requestStoragePermission(this@AppWebViewActivity, PermissionUtils.REQUEST_CODE_STORAGE_PERMISSION)
                        }
                        return true
                    }

                    // 3. 내부 웹뷰 처리 도메인 (pomission.com)
                    (url.startsWith("http://") || url.startsWith("https://")) && url.contains("pomission.com") -> {
                        return false // 그대로 WebView에 로딩
                    }

                    // 4. 외부 HTTP/HTTPS 링크는 외부 브라우저
                    url.startsWith("http://") || url.startsWith("https://") -> {
                        runExternalBrowser(url)
                        return true
                    }

                    // 5. 외부 앱 스킴 처리 (예: kakao://, naversearchapp:// 등)
                    else -> {
                        try {
                            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                            if (intent != null) {
                                // 앱이 설치되어 있으면 실행
                                if (intent.resolveActivity(packageManager) != null) {
                                    startActivity(intent)
                                    return true
                                } else {
                                    // fallback URL이 있으면 WebView로 로드
                                    val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                                    if (!fallbackUrl.isNullOrEmpty()) {
                                        view?.loadUrl(fallbackUrl)
                                        return true
                                    }

                                    // Play Store 이동
                                    val packageName = intent.`package`
                                    if (!packageName.isNullOrEmpty()) {
                                        val marketIntent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse("market://details?id=$packageName")
                                        }
                                        startActivity(marketIntent)
                                        return true
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
    }

    private val mWebViewClient: WebViewClient = object : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            onWebViewPageStartedWithTune720(view, url)
            super.onPageStarted(view, url, favicon)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url.toString()
            Timber.d("shouldOverrideUrlLoading request : $url")

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
            } else if (!url.contains("app.shoplus.io")) {
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    view?.context?.startActivity(this)
                }
                return true
            }

            return false
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            Timber.d("shouldOverrideUrlLoading url : $url")
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
                } else if (!url.contains("app.shoplus.io")) {
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

    private fun downloadImage(url: String) {
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setDescription(resources.getString(R.string.web_view_downloading))
            setTitle(URLUtil.guessFileName(url, null, null))
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, null, null))
        }

        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)

        Toast.makeText(this@AppWebViewActivity, resources.getString(R.string.web_view_download_start), Toast.LENGTH_SHORT).show()
    }

    private fun runExternalBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PermissionUtils.REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadImage(downloadUrl)
            } else {
                Toast.makeText(this, resources.getString(R.string.permissions_need_storage), Toast.LENGTH_SHORT).show()
            }
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
        setActionBar()
        setWebView()

        onBackPressedDispatcher.addCallback(this@AppWebViewActivity, onBackPressedCallback)

        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "샵플러스 쇼핑적립")
            putString(FirebaseAnalyticsManager.START_POINT, "inApp")
        })
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
        }
    }

    private fun setActionBar() {
        if (viewType == ViewType.POMISSION_ZONE.name) {
            initActionBar(
                viewDataBinding.iActionBar,
                R.string.pomission_zone_title,
                ActionBarLeftButtonEnum.BACK_BUTTON
            )
        } else {
            viewDataBinding.iActionBar.root.visibility = View.GONE
        }
    }

    private fun setWebView() {
        viewDataBinding.webView.apply {
            setPadding(0, 0, 0, 0)

            with(settings) {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true

                if (viewType == ViewType.POMISSION_ZONE.name) {
                    domStorageEnabled = true
                    loadsImagesAutomatically = true
                    allowFileAccess = true
                    allowContentAccess = true
                }
            }

            if (viewType == ViewType.POMISSION_ZONE.name) {
                webViewClient = pomissionZoneWebViewClient
                webChromeClient = pomissionZoneWebChromeClient
                addJavascriptInterface(PomissionZoneAndroidBridge(this@AppWebViewActivity), BRIDGE_NAME)
            } else {
                webViewClient = mWebViewClient
                addJavascriptInterface(AndroidBridge(this@AppWebViewActivity), BRIDGE_NAME)
            }

            setupWebViewWithTune720(viewDataBinding.webView, loadUrl)
            loadUrl(loadUrl)
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

    inner class AndroidBridge(val context: Context) {

        @JavascriptInterface
        fun close() {
            this@AppWebViewActivity.finish()
        }

        @JavascriptInterface
        fun startAutoRun(msg: String) {

        }

        @JavascriptInterface
        fun arrivedBottom(step: String) {

        }
    }

    inner class PomissionZoneAndroidBridge(val context: Context) {

        /**
         * pomission zone 연동 interface
         */
        @JavascriptInterface
        fun showMessage(message: String) {
            CustomToast.showToast(context, message)
        }

        /**
         * pomission zone 연동 interface
         */
        @JavascriptInterface
        fun isParticipated(result: String) {
            Timber.tag("PomissionZone").d("event success : $result")
        }
    }
}