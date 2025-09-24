package com.moneyweather.extensions

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import com.moneyweather.util.webview.CustomWebChromeClient

@SuppressLint("SetJavaScriptEnabled")
fun WebView.applyBottomAdBannerSettings(client: WebViewClient = WebViewClient()): WebView =
    apply {
        layoutParams = LayoutParams(
            0,
            LayoutParams.WRAP_CONTENT
        ).apply {
            bottomToBottom = LayoutParams.PARENT_ID
            startToStart = LayoutParams.PARENT_ID
            endToEnd = LayoutParams.PARENT_ID
        }

        setBackgroundColor(Color.TRANSPARENT)

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webViewClient = client
        webChromeClient = CustomWebChromeClient()
    }

@SuppressLint("SetJavaScriptEnabled")
fun WebView.applyAdBannerSettings(client: WebViewClient = WebViewClient()): WebView =
    apply {
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        setBackgroundColor(Color.TRANSPARENT)
        // 배경 깜박임 현상 제거를 위해 하드웨어 가속을 끔
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webViewClient = client
    }