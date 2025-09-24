package com.moneyweather.model.enums

enum class BannerType(val type: String) {
    ADPIE("adpie"),
    MOBWITH("mobwith");

    companion object {
        fun fromType(type: String): BannerType? = values().find { it.type == type }
    }
}