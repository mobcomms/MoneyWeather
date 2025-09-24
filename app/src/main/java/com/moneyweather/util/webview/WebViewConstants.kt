package com.moneyweather.util.webview


object UrlSchemePrefix {
    const val HTTP = "http://"
    const val HTTPS = "https://"
    const val INTENT = "intent://"

    object Coupang {
        const val DEEP_LINK = "coupang://"
        const val PRODUCT_DEEP_LINK = "${DEEP_LINK}product/"
        const val EVENT_DEEP_LINK = "${DEEP_LINK}event/"
        const val WEB_URL = "https://www.coupang.com/"
        const val PRODUCT_WEB_URL = "${WEB_URL}vp/products/"
        const val EVENT_WEB_URL = "${WEB_URL}np/events/"
    }
}