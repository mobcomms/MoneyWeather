package com.moneyweather.extensions

fun Boolean.toYN(): String = if (this) "Y" else "N"

fun String?.toBooleanYN(): Boolean = this.equals("Y", ignoreCase = true)