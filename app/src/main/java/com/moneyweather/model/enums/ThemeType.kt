package com.moneyweather.model.enums


enum class ThemeType(val type: Int) {
    INFO(0),  // 정보형
    SIMPLE(1),  // 심플형
    CALENDAR(2),  // 달력형
    BACKGROUND(3), // 배경형
    VIDEO(4); // 영상형

    companion object {
        fun fromInt(value: Int) = ThemeType.values().first { it.type == value }
    }
}