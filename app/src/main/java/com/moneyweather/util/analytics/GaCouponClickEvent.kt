package com.moneyweather.util.analytics

import com.moneyweather.util.analytics.FirebaseEventUtil.CtaType
import com.moneyweather.util.analytics.FirebaseEventUtil.EventName
import com.moneyweather.util.analytics.FirebaseEventUtil.GroupId
import com.moneyweather.util.analytics.FirebaseEventUtil.KeyName
import com.moneyweather.util.analytics.FirebaseEventUtil.ScreenName
import com.moneyweather.util.analytics.FirebaseEventUtil.getUserType
import com.moneyweather.util.analytics.FirebaseEventUtil.logCustomEvent

object GaCouponClickEvent {
    private fun logCouponClickEvent(couponName: String) {
        logCustomEvent(
            EventName.COUPON_CLICK,
            mapOf(
                KeyName.GROUP_ID to GroupId.HOME_COUPON,
                KeyName.COUPON_ID to couponName,
                KeyName.SCREEN_NAME to ScreenName.HOME,
                KeyName.CTA_TYPE to CtaType.NAVIGATE_TO_COUPON_DETAIL,
                KeyName.USER_TYPE to getUserType(),
            )
        )
    }

    fun logCouponNpay(name: String) {
        when (name.trim()) {
            "Npay 3,000원" -> logCouponNpay3000()
            "Npay 5,000원" -> logCouponNpay5000()
            "Npay 10,000원" -> logCouponNpay10000()
            "Npay 20,000원" -> logCouponNpay20000()
            "Npay 30,000원" -> logCouponNpay30000()
            "Npay 50,000원" -> logCouponNpay50000()
            else -> logCouponClickEvent(couponName = "coupon_home_npay")
        }

    }

    private fun logCouponNpay3000() {
        logCouponClickEvent(couponName = "coupon_home_npay3000")
    }

    private fun logCouponNpay5000() {
        logCouponClickEvent(couponName = "coupon_home_npay5000")
    }

    private fun logCouponNpay10000() {
        logCouponClickEvent(couponName = "coupon_home_npay10000")
    }

    private fun logCouponNpay20000() {
        logCouponClickEvent(couponName = "coupon_home_npay20000")
    }

    private fun logCouponNpay30000() {
        logCouponClickEvent(couponName = "coupon_home_npay30000")
    }

    private fun logCouponNpay50000() {
        logCouponClickEvent(couponName = "coupon_home_npay50000")
    }
}