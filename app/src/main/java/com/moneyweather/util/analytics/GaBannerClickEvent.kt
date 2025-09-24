package com.moneyweather.util.analytics

import com.moneyweather.util.analytics.FirebaseEventUtil.CtaType
import com.moneyweather.util.analytics.FirebaseEventUtil.EventName
import com.moneyweather.util.analytics.FirebaseEventUtil.GroupId
import com.moneyweather.util.analytics.FirebaseEventUtil.KeyName
import com.moneyweather.util.analytics.FirebaseEventUtil.ScreenName
import com.moneyweather.util.analytics.FirebaseEventUtil.getUserType
import com.moneyweather.util.analytics.FirebaseEventUtil.logCustomEvent

object GaBannerClickEvent {
    private fun logBannerClickEvent(groupName: String, bannerName: String, screenName: String, ctaType: String) {
        logCustomEvent(
            EventName.BANNER_CLICK,
            mapOf(
                KeyName.GROUP_ID to groupName,
                KeyName.BANNER_ID to bannerName,
                KeyName.SCREEN_NAME to screenName,
                KeyName.CTA_TYPE to ctaType,
                KeyName.USER_TYPE to getUserType(),
            )
        )
    }

    // region Home Banner Click Event
    private fun logHomeBannerClickEvent(bannerName: String, ctaType: String) {
        logBannerClickEvent(
            groupName = GroupId.HOME_BANNER,
            bannerName = bannerName,
            screenName = ScreenName.HOME,
            ctaType = ctaType
        )
    }

    fun logHomeBannerPomissionClickEvent() {
        logHomeBannerClickEvent(
            bannerName = "banner_home_offerwall_pomission",
            ctaType = CtaType.NAVIGATE_TO_OFFERWALL_POMISSION
        )
    }
    // endregion

    // region Shop Banner Click Event
    private fun logInviteBannerClickEvent(bannerName: String, screenName: String) {
        logBannerClickEvent(
            groupName = GroupId.INVITE_BANNER,
            bannerName = bannerName,
            screenName = screenName,
            ctaType = CtaType.NAVIGATE_TO_INVITE
        )
    }

    fun logInviteBannerHomeClickEvent() {
        logInviteBannerClickEvent(
            bannerName = "banner_home_invite",
            screenName = ScreenName.HOME
        )
    }

    fun logInviteBannerMyPageClickEvent() {
        logInviteBannerClickEvent(
            bannerName = "banner_mypage_invite",
            screenName = ScreenName.MY_PAGE
        )
    }

    fun logInviteBannerSettingClickEvent() {
        logInviteBannerClickEvent(
            bannerName = "banner_setting_invite",
            screenName = ScreenName.SETTING
        )
    }
    // endregion

    // region Offerwall Banner Click Event
    private fun logOfferwallBannerClickEvent(bannerName: String, ctaType: String) {
        logBannerClickEvent(
            groupName = GroupId.OFFERWALL_BANNER,
            bannerName = bannerName,
            screenName = ScreenName.OFFERWALL,
            ctaType = ctaType
        )
    }

    fun logOfferwallBannerPomissionClickEvent() {
        logOfferwallBannerClickEvent(
            bannerName = "banner_offerwall_pomission",
            ctaType = CtaType.NAVIGATE_TO_OFFERWALL_POMISSION
        )
    }

    fun logOfferwallBannerTnkClickEvent() {
        logOfferwallBannerClickEvent(
            bannerName = "banner_offerwall_tnk",
            ctaType = CtaType.NAVIGATE_TO_OFFERWALL_TNK
        )
    }

    fun logOfferwallBannerBuzzvilClickEvent() {
        logOfferwallBannerClickEvent(
            bannerName = "banner_offerwall_buzzvil",
            ctaType = CtaType.NAVIGATE_TO_OFFERWALL_BUZZVIL
        )
    }

    fun logOfferwallBannerPincruxClickEvent() {
        logOfferwallBannerClickEvent(
            bannerName = "banner_offerwall_pincrux",
            ctaType = CtaType.NAVIGATE_TO_OFFERWALL_PINCRUX
        )
    }

    fun logOfferwallBannerAdpopcornClickEvent() {
        logOfferwallBannerClickEvent(
            bannerName = "banner_offerwall_adpopcorn",
            ctaType = CtaType.NAVIGATE_TO_OFFERWALL_ADPOPCORN
        )
    }

    fun logOfferwallBannerNasmediaClickEvent() {
        logOfferwallBannerClickEvent(
            bannerName = "banner_offerwall_nasmedia",
            ctaType = CtaType.NAVIGATE_TO_OFFERWALL_NASMEDIA
        )
    }

    // endregion
}