package com.moneyweather.extensions

import androidx.core.text.HtmlCompat

fun String.toHtmlText() =
    HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)