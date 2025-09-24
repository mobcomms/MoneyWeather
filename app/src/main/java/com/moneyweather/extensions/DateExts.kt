package com.moneyweather.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.getNotificationTime(): String =
    SimpleDateFormat("a h:mm", Locale.KOREAN).format(this)