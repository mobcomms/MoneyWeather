package com.moneyweather.util.analytics

import com.moneyweather.util.analytics.FirebaseEventUtil.AdNetwork
import com.moneyweather.util.analytics.FirebaseEventUtil.CtaType
import com.moneyweather.util.analytics.FirebaseEventUtil.EventName
import com.moneyweather.util.analytics.FirebaseEventUtil.GroupId
import com.moneyweather.util.analytics.FirebaseEventUtil.KeyName
import com.moneyweather.util.analytics.FirebaseEventUtil.ScreenName
import com.moneyweather.util.analytics.FirebaseEventUtil.getUserType
import com.moneyweather.util.analytics.FirebaseEventUtil.logCustomEvent

object GaAdBannerClickEvent {
    private fun logAdBannerClickEvent(adBannerName: String, adNetwork: String) {
        logCustomEvent(
            EventName.AD_BANNER_CLICK,
            mapOf(
                KeyName.GROUP_ID to GroupId.LOCKSCREEN_AD_BANNER,
                KeyName.AD_BANNER_ID to adBannerName,
                KeyName.SCREEN_NAME to ScreenName.LOCKSCREEN,
                KeyName.CTA_TYPE to CtaType.LAUNCH_AD,
                KeyName.USER_TYPE to getUserType(),
                KeyName.AD_NETWORK to adNetwork
            )
        )
    }

    fun logLockScreenAdpieBannerClickEvent() {
        logAdBannerClickEvent(
            adBannerName = "ad_banner_lockscreen_adpie",
            adNetwork = AdNetwork.ADPIE
        )
    }

    fun logLockScreenMobwithClickEvent() {
        logAdBannerClickEvent(
            adBannerName = "ad_banner_lockscreen_mobwith",
            adNetwork = AdNetwork.MOBWITH
        )
    }
}