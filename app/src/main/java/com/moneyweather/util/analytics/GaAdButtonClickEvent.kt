package com.moneyweather.util.analytics

import com.moneyweather.util.analytics.FirebaseEventUtil.CtaType
import com.moneyweather.util.analytics.FirebaseEventUtil.EventName
import com.moneyweather.util.analytics.FirebaseEventUtil.GroupId
import com.moneyweather.util.analytics.FirebaseEventUtil.KeyName
import com.moneyweather.util.analytics.FirebaseEventUtil.ScreenName
import com.moneyweather.util.analytics.FirebaseEventUtil.getUserType
import com.moneyweather.util.analytics.FirebaseEventUtil.logCustomEvent

object GaAdButtonClickEvent {
    private fun logAdButtonClickEvent(adButtonName: String, adNetwork: String) {
        logCustomEvent(
            EventName.AD_BUTTON_CLICK,
            mapOf(
                KeyName.GROUP_ID to GroupId.LOCKSCREEN_BUTTON,
                KeyName.AD_BUTTON_ID to adButtonName,
                KeyName.SCREEN_NAME to ScreenName.LOCKSCREEN,
                KeyName.CTA_TYPE to CtaType.LAUNCH_AD,
                KeyName.USER_TYPE to getUserType(),
                KeyName.AD_NETWORK to adNetwork
            )
        )
    }

    fun logAdButtonCoupangCpsClickEventCoupang() {
        logAdButtonClickEvent(
            adButtonName = "ad_button_lockscreen_coupangcps",
            adNetwork = FirebaseEventUtil.AdNetwork.COUPANG
        )
    }

    fun logAdButtonMobonLiveClickEventCoupang() {
        logAdButtonClickEvent(
            adButtonName = "ad_button_lockscreen_mobonlive",
            adNetwork = FirebaseEventUtil.AdNetwork.MOBON_LIVE
        )
    }

    fun logAdButtonPomissionClickEventCoupang() {
        logAdButtonClickEvent(
            adButtonName = "ad_button_lockscreen_pomission",
            adNetwork = FirebaseEventUtil.AdNetwork.POMISSION
        )
    }

    fun logAdButtonMobonDongdongClickEventCoupang() {
        logAdButtonClickEvent(
            adButtonName = "ad_button_lockscreen_mobondongdong",
            adNetwork = FirebaseEventUtil.AdNetwork.MOBON_DONGDONG
        )
    }


}