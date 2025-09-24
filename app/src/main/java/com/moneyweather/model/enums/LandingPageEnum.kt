package com.moneyweather.model.enums

enum class LandingPageEnum(val type: Int, val path: String) {
    HOME(0, "/home"),
    CHARGE(1, "/charge"),
    STORE(2, "/store"),
    MY(3, "/my"),
    MYPOINT(4, "/mypoint"),
    SETTING(5, "/setting")
}