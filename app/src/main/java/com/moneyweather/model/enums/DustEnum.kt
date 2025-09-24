package com.moneyweather.model.enums

import com.moneyweather.R

enum class DustEnum (val value: String, val res : Int) {

    FINE("좋음", R.color.dust_fine_color),
    NORMAL("보통", R.color.dust_normal_color),
    BAD("나쁨", R.color.dust_bad_color),
    VERY_BAD("나쁨", R.color.dust_very_bad_color),
}