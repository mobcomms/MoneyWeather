package com.moneyweather.util.analytics

import com.moneyweather.util.analytics.FirebaseEventUtil.EventName
import com.moneyweather.util.analytics.FirebaseEventUtil.GroupId
import com.moneyweather.util.analytics.FirebaseEventUtil.KeyName
import com.moneyweather.util.analytics.FirebaseEventUtil.ScreenName
import com.moneyweather.util.analytics.FirebaseEventUtil.getUserType
import com.moneyweather.util.analytics.FirebaseEventUtil.logCustomEvent

object GaIconClickEvent {

    private fun logLockScreenIconClickEvent(iconName: String, ctaType: String) {
        logCustomEvent(
            EventName.ICON_CLICK,
            mapOf(
                KeyName.GROUP_ID to GroupId.LOCKSCREEN_ICON,
                KeyName.ICON_ID to iconName,
                KeyName.SCREEN_NAME to ScreenName.LOCKSCREEN,
                KeyName.CTA_TYPE to ctaType,
                KeyName.USER_TYPE to getUserType(),

                )
        )
    }

    fun logLockScreenOfferwallPomissionIconClickEvent() {
        logLockScreenIconClickEvent(
            iconName = FirebaseEventUtil.LockScreenIconName.OFFERWALL_POMISSION,
            ctaType = FirebaseEventUtil.CtaType.NAVIGATE_TO_OFFERWALL_POMISSION
        )
    }

    fun logLockScreenShopRewardIconClickEvent() {
        logLockScreenIconClickEvent(
            iconName = FirebaseEventUtil.LockScreenIconName.SHOP_REWARD,
            ctaType = FirebaseEventUtil.CtaType.NAVIGATE_TO_SHOP_REWARD
        )
    }

    fun logLockScreenGameIconClickEvent() {
        logLockScreenIconClickEvent(
            iconName = FirebaseEventUtil.LockScreenIconName.GAME,
            ctaType = FirebaseEventUtil.CtaType.NAVIGATE_TO_GAMEZONE
        )
    }

    fun logLockScreenOfferwallIconClickEvent() {
        logLockScreenIconClickEvent(
            iconName = FirebaseEventUtil.LockScreenIconName.OFFERWALL,
            ctaType = FirebaseEventUtil.CtaType.NAVIGATE_TO_OFFERWALL
        )
    }

    fun logLockScreenThemeSettingIconClickEvent() {
        logLockScreenIconClickEvent(
            iconName = FirebaseEventUtil.LockScreenIconName.THEME_SETTING,
            ctaType = FirebaseEventUtil.CtaType.SETTING
        )
    }

    fun logLockScreenNaverWeatherIconClickEvent() {
        logLockScreenIconClickEvent(
            iconName = FirebaseEventUtil.LockScreenIconName.NAVER_WEATHER,
            ctaType = FirebaseEventUtil.CtaType.OPEN_URL
        )
    }
}