package com.moneyweather.util.analytics

import com.moneyweather.util.analytics.FirebaseEventUtil.EventName
import com.moneyweather.util.analytics.FirebaseEventUtil.GroupId
import com.moneyweather.util.analytics.FirebaseEventUtil.KeyName
import com.moneyweather.util.analytics.FirebaseEventUtil.ScreenName
import com.moneyweather.util.analytics.FirebaseEventUtil.getUserType
import com.moneyweather.util.analytics.FirebaseEventUtil.logCustomEvent

object GaButtonClickEvent {

    private fun logButtonClickEvent(groupName: String, buttonName: String, screenName: String, ctaType: String) {
        logCustomEvent(
            EventName.BUTTON_CLICK,
            mapOf(
                KeyName.GROUP_ID to groupName,
                KeyName.BUTTON_ID to buttonName,
                KeyName.SCREEN_NAME to screenName,
                KeyName.CTA_TYPE to ctaType,
                KeyName.USER_TYPE to getUserType(),
            )
        )
    }

    // region LockScreen Button Click Event
    private fun logLockScreenButtonClickEvent(buttonName: String, ctaType: String) {
        logButtonClickEvent(
            groupName = GroupId.LOCKSCREEN_BUTTON,
            buttonName = buttonName,
            screenName = ScreenName.LOCKSCREEN,
            ctaType = ctaType
        )
    }

    fun logLockScreenRewardSunflowerButtonClickEvent() {
        logLockScreenButtonClickEvent(
            buttonName = "button_lockscreen_rewardsunflower",
            ctaType = FirebaseEventUtil.CtaType.RECEIVE_REWARD
        )
    }

    fun logLockScreenOfferwallTnkVideoButtonClickEvent() {
        logLockScreenButtonClickEvent(
            buttonName = "button_lockscreen_offerwall_tnk_video",
            ctaType = FirebaseEventUtil.CtaType.NAVIGATE_TO_OFFERWALL_TNK
        )
    }

    fun logLockScreenOfferwallTnkQuizButtonClickEvent() {
        logLockScreenButtonClickEvent(
            buttonName = "button_lockscreen_offerwall_tnk_quiz",
            ctaType = FirebaseEventUtil.CtaType.NAVIGATE_TO_OFFERWALL_TNK
        )
    }

    fun logLockScreenOfferwallTnkNewsButtonClickEvent() {
        logLockScreenButtonClickEvent(
            buttonName = "button_lockscreen_offerwall_tnk_news",
            ctaType = FirebaseEventUtil.CtaType.NAVIGATE_TO_OFFERWALL_TNK
        )
    }
    // endregion

    // region Home Button Click Event
    private fun logHomeButtonClickEvent(buttonName: String, ctaType: String) {
        logButtonClickEvent(
            groupName = GroupId.HOME_BUTTON,
            buttonName = buttonName,
            screenName = ScreenName.HOME,
            ctaType = ctaType
        )
    }

    fun logHomeShopRewardButtonClickEvent() {
        logHomeButtonClickEvent(
            buttonName = "button_home_shopreward",
            ctaType = FirebaseEventUtil.CtaType.NAVIGATE_TO_SHOP_REWARD
        )
    }

    fun logHomeGameButtonClickEvent() {
        logHomeButtonClickEvent(
            buttonName = "button_home_game",
            ctaType = FirebaseEventUtil.CtaType.NAVIGATE_TO_GAME
        )
    }

    fun logHomeOfferwallButtonClickEvent() {
        logHomeButtonClickEvent(
            buttonName = "button_home_offerwall",
            ctaType = FirebaseEventUtil.CtaType.NAVIGATE_TO_OFFERWALL
        )
    }

    fun logHomeShopMoreButtonClickEvent() {
        logHomeButtonClickEvent(
            buttonName = "button_home_shop_more",
            ctaType = FirebaseEventUtil.CtaType.NAVIGATE_TO_SHOP
        )
    }
    // endregion

    // region Tab Button Click Event
    private fun logTabButtonClickEvent(buttonName: String, screenName: String, ctaType: String) {
        logButtonClickEvent(
            groupName = GroupId.TAB_BUTTON,
            buttonName = buttonName,
            screenName = screenName,
            ctaType = ctaType
        )
    }

    fun logMainTabButtonClickEvent(currentTabIndex: Int, moveTabIndex: Int) {
        val buttonName = when (moveTabIndex) {
            0 -> "button_tab_home"
            1 -> "button_tab_offerwall"
            2 -> "button_tab_shop"
            3 -> "button_tab_mypage"
            else -> ""
        }

        val currentTabName = when (currentTabIndex) {
            0 -> FirebaseEventUtil.MainTabType.HOME.type
            1 -> FirebaseEventUtil.MainTabType.OFFERWALL.type
            2 -> FirebaseEventUtil.MainTabType.SHOP.type
            3 -> FirebaseEventUtil.MainTabType.MY_PAGE.type
            else -> ""
        }

        val ctaType = when (moveTabIndex) {
            0 -> FirebaseEventUtil.CtaType.NAVIGATE_TO_HOME
            1 -> FirebaseEventUtil.CtaType.NAVIGATE_TO_OFFERWALL
            2 -> FirebaseEventUtil.CtaType.NAVIGATE_TO_SHOP
            3 -> FirebaseEventUtil.CtaType.NAVIGATE_TO_MY_PAGE
            else -> ""
        }

        logTabButtonClickEvent(
            buttonName = buttonName,
            screenName = currentTabName,
            ctaType = ctaType
        )
    }

    // endregion
}