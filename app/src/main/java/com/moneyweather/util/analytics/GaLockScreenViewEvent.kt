package com.moneyweather.util.analytics

import com.moneyweather.util.analytics.FirebaseEventUtil.EventName
import com.moneyweather.util.analytics.FirebaseEventUtil.KeyName
import com.moneyweather.util.analytics.FirebaseEventUtil.LockScreenType
import com.moneyweather.util.analytics.FirebaseEventUtil.getUserType
import com.moneyweather.util.analytics.FirebaseEventUtil.logCustomEvent

object GaLockScreenViewEvent {

    private fun logLockScreenViewEvent(lockScreenType: LockScreenType) {
        logCustomEvent(
            EventName.LOCKSCREEN_VIEW,
            mapOf(
                KeyName.THEME_ID to lockScreenType.type,
                KeyName.USER_TYPE to getUserType()
            )
        )
    }

    fun logLockScreenInfoViewEvent() {
        logLockScreenViewEvent(LockScreenType.INFO)
    }

    fun logLockScreenSimpleViewEvent() {
        logLockScreenViewEvent(LockScreenType.SIMPLE)
    }

    fun logLockScreenCalendarViewEvent() {
        logLockScreenViewEvent(LockScreenType.CALENDAR)
    }

    fun logLockScreenBackgroundViewEvent() {
        logLockScreenViewEvent(LockScreenType.BACKGROUND)
    }

    fun logLockScreenVideoViewEvent() {
        logLockScreenViewEvent(LockScreenType.VIDEO)
    }

    fun logLockScreenThemeViewEvent(themeType: Int) {
        when (themeType) {
            0 -> logLockScreenInfoViewEvent()
            1 -> logLockScreenSimpleViewEvent()
            2 -> logLockScreenCalendarViewEvent()
            3 -> logLockScreenBackgroundViewEvent()
            4 -> logLockScreenVideoViewEvent()
        }
    }
}