package com.moneyweather.util.analytics

import com.moneyweather.util.analytics.FirebaseEventUtil.EventName
import com.moneyweather.util.analytics.FirebaseEventUtil.GroupId
import com.moneyweather.util.analytics.FirebaseEventUtil.KeyName
import com.moneyweather.util.analytics.FirebaseEventUtil.ScreenName
import com.moneyweather.util.analytics.FirebaseEventUtil.getUserType
import com.moneyweather.util.analytics.FirebaseEventUtil.logCustomEvent

object GaToggleClickEvent {
    private fun logSettingToggleClickEvent(toggleName: String, isOn: Boolean) {
        logCustomEvent(
            EventName.TOGGLE_CLICK,
            mapOf(
                KeyName.GROUP_ID to GroupId.SETTING_TOGGLE,
                KeyName.TOGGLE_ID to toggleName,
                KeyName.SCREEN_NAME to ScreenName.SETTING,
                KeyName.USER_TYPE to getUserType(),
                KeyName.TOGGLE_STATUS to if (isOn) "on" else "off",

                )
        )
    }

    fun logSettingToggleLockScreenClickEvent(isOn: Boolean) {
        logSettingToggleClickEvent(
            toggleName = "toggle_setting_lockscreen",
            isOn = isOn
        )
    }
}