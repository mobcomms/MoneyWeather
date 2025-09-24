package com.moneyweather.util.analytics

import com.moneyweather.util.analytics.FirebaseEventUtil.AdNetwork
import com.moneyweather.util.analytics.FirebaseEventUtil.CtaType
import com.moneyweather.util.analytics.FirebaseEventUtil.EventName
import com.moneyweather.util.analytics.FirebaseEventUtil.GroupId
import com.moneyweather.util.analytics.FirebaseEventUtil.KeyName
import com.moneyweather.util.analytics.FirebaseEventUtil.ScreenName
import com.moneyweather.util.analytics.FirebaseEventUtil.getUserType
import com.moneyweather.util.analytics.FirebaseEventUtil.logCustomEvent

object GaAdNewsClickEvent {

    fun logAdNewsClickEvent() {
        logCustomEvent(
            EventName.AD_NEWS_CLICK,
            mapOf(
                KeyName.GROUP_ID to GroupId.LOCKSCREEN_BUTTON,
                KeyName.AD_BUTTON_ID to "ad_button_lockscreen_news",
                KeyName.SCREEN_NAME to ScreenName.LOCKSCREEN,
                KeyName.CTA_TYPE to CtaType.LAUNCH_AD,
                KeyName.USER_TYPE to getUserType(),
                KeyName.AD_NETWORK to AdNetwork.ONE_MINUTE
            )
        )
    }
}