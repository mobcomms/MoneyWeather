package com.moneyweather.util.webview

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class BannerWebViewClient(
    val onUrlLoading: (view: WebView?, url: String?) -> Unit = { _, _ -> },
    val onFinished: (view: WebView?, url: String?) -> Unit = { _, _ -> },
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val context = view?.context ?: return false
        val url = request?.url?.toString() ?: return false

        onUrlLoading(view, url)

        when {
            url.startsWith(UrlSchemePrefix.HTTPS) || url.startsWith(UrlSchemePrefix.HTTP) -> {
                openExternalBrowser(context, url)
            }

            url.startsWith(UrlSchemePrefix.INTENT) -> {
                openIntentScheme(
                    context = context,
                    url = url
                )
            }

            else -> {
                openDeepLink(
                    context = context,
                    url = url
                )
            }
        }

        return true
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        onFinished(view, url)
    }

    private fun openIntentScheme(context: Context, url: String) {
        try {
            context.startActivity(Intent.parseUri(url, Intent.URI_INTENT_SCHEME).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: ActivityNotFoundException) {
            // 앱이 설치되지 않은 경우, 대체 URL 처리
            e.printStackTrace()
        }
    }

    private fun openExternalBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    private fun openDeepLink(context: Context, url: String) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } catch (e: ActivityNotFoundException) {
            // 앱이 설치되지 않은 경우, 대체 URL 처리
            e.printStackTrace()
        }
    }

    private fun getFallbackUrl(url: String): String {
        return when {
            url.startsWith(UrlSchemePrefix.Coupang.DEEP_LINK) -> {
                if (url.startsWith(UrlSchemePrefix.Coupang.PRODUCT_DEEP_LINK)) {
                    val productId = url.substringAfter(UrlSchemePrefix.Coupang.PRODUCT_DEEP_LINK)
                    "${UrlSchemePrefix.Coupang.PRODUCT_WEB_URL}$productId"
                } else if (url.startsWith(UrlSchemePrefix.Coupang.EVENT_DEEP_LINK)) {
                    val eventId = url.substringAfter(UrlSchemePrefix.Coupang.EVENT_DEEP_LINK)
                    "${UrlSchemePrefix.Coupang.EVENT_WEB_URL}$eventId"
                } else {
                    UrlSchemePrefix.Coupang.WEB_URL
                }
            }

            else -> url
        }
    }
}