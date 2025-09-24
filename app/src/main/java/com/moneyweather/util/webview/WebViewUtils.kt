package com.moneyweather.util.webview

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.WebView
import com.moneyweather.util.Logger

object WebViewUtils {

    fun isSchemeUrl(url: String): Boolean {
        return (url.startsWith("intent://") || url.startsWith("elevenst://")
                || url.startsWith("aliexpress://") || url.startsWith("coupang://")
                || url.startsWith("yanoljamotel://") || url.startsWith("market://")
                || url.startsWith("boribori://"))
    }

    fun launchIntentScheme(webView: WebView, url: String) {
        try {
            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            val packageManager = webView.context?.packageManager
            val resolveInfo = packageManager?.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

            // fixme: 2024-11-14
            /*
                "쇼핑하고 적립받기" 클릭 시 링크프라이스 또는 도트피치로 이동된 후 쇼핑몰로 이동하는 구조인데
                스킴 url로 이동하는 경우에도 shouldOverrideUrlLoading 메소드 내에서 https 페이지가 감지되어
                내부 브라우저로 해당 페이지 이동하면서 외부로 이동하기 때문에
                요구사항에 맞게 임시로 앱에서 goBack() 처리 (임시방편으로 처리한 것이므로 추후 수정 필요)
             */
            if(resolveInfo != null) {
                webView.context.startActivity(intent)
                webView.goBack()
            }else{
                val fallbackParams = listOf("browser_fallback_url", "link", "afl")
                var fallbackUrl: String? = null

                for (param in fallbackParams) {
                    fallbackUrl = intent.getStringExtra(param)
                    if (fallbackUrl != null) break
                }

                if (fallbackUrl != null) {
                    webView.loadUrl(fallbackUrl)
                } else {
                    webView.context.startActivity(intent)
                    webView.goBack()
                }
            }
        } catch (e: Throwable) {
            Logger.e(e.message)
        }
    }

    fun openExternalBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}