package com.moneyweather.util.webview

import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import timber.log.Timber

class CustomWebChromeClient : WebChromeClient() {
    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        Timber.tag(TAG).d("console: ${consoleMessage?.message()}")

        return super.onConsoleMessage(consoleMessage)
    }

    companion object {
        const val TAG = "WebChromeClient"
    }
}