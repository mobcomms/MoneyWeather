package com.moneyweather.util.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.moneyweather.BuildConfig
import com.moneyweather.util.PrefRepository
import timber.log.Timber

object FirebaseEventUtil {

    //region Constants
    object KeyName {
        const val GROUP_ID = "group_id"
        const val SCREEN_NAME = "screen_name"
        const val CTA_TYPE = "cta_type"
        const val USER_TYPE = "user_type"
        const val AD_NETWORK = "ad_network"
        const val TOGGLE_STATUS = "toggle_status"

        const val THEME_ID = "theme_id"
        const val ICON_ID = "icon_id"
        const val AD_BUTTON_ID = "ad_button_id"
        const val BUTTON_ID = "button_id"
        const val AD_BANNER_ID = "ad_banner_id"
        const val BANNER_ID = "banner_id"
        const val COUPON_ID = "coupon_id"
        const val TOGGLE_ID = "toggle_id"
    }

    object EventName {
        const val LOCKSCREEN_VIEW = "lockscreen_view"
        const val ICON_CLICK = "icon_click"
        const val AD_BUTTON_CLICK = "ad_button_click"
        const val AD_NEWS_CLICK = "ad_news_click"
        const val BUTTON_CLICK = "button_click"
        const val AD_BANNER_CLICK = "ad_banner_click"
        const val BANNER_CLICK = "banner_click"
        const val COUPON_CLICK = "coupon_click"
        const val TOGGLE_CLICK = "toggle_click"
    }

    object GroupId {
        const val LOCKSCREEN_ICON = "lockscreen_icon"
        const val LOCKSCREEN_BUTTON = "lockscreen_button"
        const val HOME_BUTTON = "home_button"
        const val TAB_BUTTON = "tab_button"
        const val LOCKSCREEN_AD_BANNER = "lockscreen_ad_banner"
        const val HOME_BANNER = "home_banner"
        const val INVITE_BANNER = "invite_banner"
        const val OFFERWALL_BANNER = "offerwall_banner"
        const val HOME_COUPON = "home_coupon"
        const val SETTING_TOGGLE = "setting_toggle"
    }

    object ScreenName {
        const val LOCKSCREEN = "lockscreen"
        const val HOME = "home"
        const val OFFERWALL = "offerwall"
        const val SHOP = "shop"
        const val MY_PAGE = "mypage"
        const val SETTING = "setting"
    }

    object LockScreenIconName {
        const val OFFERWALL_POMISSION = "icon_lockscreen_offerwall_pomission"
        const val SHOP_REWARD = "icon_lockscreen_shopreward"
        const val GAME = "icon_lockscreen_game"
        const val OFFERWALL = "icon_lockscreen_offerwall"
        const val THEME_SETTING = "icon_lockscreen_themesetting"
        const val NAVER_WEATHER = "icon_lockscreen_naverweather"
    }

    object CtaType {
        const val SETTING = "setting"
        const val OPEN_URL = "open_url"
        const val LAUNCH_AD = "launch_ad"
        const val RECEIVE_REWARD = "receive_reward"
        const val NAVIGATE_TO_GAME = "navigate_to_game"
        const val NAVIGATE_TO_HOME = "navigate_to_home"
        const val NAVIGATE_TO_SHOP = "navigate_to_shop"
        const val NAVIGATE_TO_MY_PAGE = "navigate_to_mypage"
        const val NAVIGATE_TO_INVITE = "navigate_to_invite"
        const val NAVIGATE_TO_SHOP_REWARD = "navigate_to_shopreward"
        const val NAVIGATE_TO_GAMEZONE = "navigate_to_gamezone"
        const val NAVIGATE_TO_OFFERWALL = "navigate_to_offerwall"
        const val NAVIGATE_TO_OFFERWALL_BUZZVIL = "navigate_to_offerwall_buzzvil"
        const val NAVIGATE_TO_OFFERWALL_POMISSION = "navigate_to_offerwall_pomission"
        const val NAVIGATE_TO_OFFERWALL_PINCRUX = "navigate_to_offerwall_pincrux"
        const val NAVIGATE_TO_OFFERWALL_TNK = "navigate_to_offerwall_tnk"
        const val NAVIGATE_TO_OFFERWALL_ADPOPCORN = "navigate_to_offerwall_adpopcorn"
        const val NAVIGATE_TO_OFFERWALL_NASMEDIA = "navigate_to_offerwall_nasmedia"
        const val NAVIGATE_TO_COUPON_DETAIL = "navigate_to_coupon_detail"
    }

    enum class LockScreenType(val type: String) {
        INFO("lockscreen_info"),
        SIMPLE("lockscreen_simple"),
        CALENDAR("lockscreen_calendar"),
        BACKGROUND("lockscreen_background"),
        VIDEO("lockscreen_video")
    }

    object AdNetwork {
        const val COUPANG = "coupang"
        const val MOBON_LIVE = "mobonlive"
        const val POMISSION = "pomission"
        const val MOBON_DONGDONG = "mobondongdong"
        const val ONE_MINUTE = "1minute"
        const val ADPIE = "adpie"
        const val MOBWITH = "mobwith"
    }

    object UserType {
        const val MEMBER = "member"
        const val GUEST = "guest"
    }

    enum class MainTabType(val type: String) {
        HOME("home"),
        OFFERWALL("offerwall"),
        SHOP("shop"),
        MY_PAGE("mypage"),
    }

    //endregion

    private val firebaseAnalytics by lazy {
        Firebase.analytics
    }

    private var currentMainTab: MainTabType = MainTabType.HOME

    fun updateCurrentMainTab(tabType: MainTabType) {
        currentMainTab = tabType
    }

    /**
     * 버튼 등 클릭 이벤트 기록
     */
    private fun logClickEvent(
        itemId: String,
        itemName: String,
        contentType: String = "button",
        customParams: Map<String, String>? = null
    ) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
            putString(FirebaseAnalytics.Param.ITEM_NAME, itemName)
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)

            customParams?.forEach { (key, value) ->
                putString(key, value)
            }
        }

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle)
    }

    /**
     * 사용자 정의 이벤트 기록
     */
    fun logCustomEvent(
        eventName: String,
        params: Map<String, String>? = null
    ) {
        Timber.tag("Firebase Analytics").d("Event : $eventName params: $params")

        if (BuildConfig.DEBUG) return

        val bundle = Bundle().apply {
            params?.forEach { (key, value) ->
                putString(key, value)
            }
        }

        firebaseAnalytics.logEvent(eventName, bundle)
    }

    fun getUserType(): String {
        return if (PrefRepository.UserInfo.isLogin) {
            UserType.MEMBER
        } else {
            UserType.GUEST
        }
    }
}