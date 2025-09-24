package com.moneyweather.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object FirebaseAnalyticsManager {

    // Param
    const val VIEW_NAME = "view_name"
    const val START_POINT = "start_point"
    const val TAB_NAME = "tab_name"
    const val LOGIN_TYPE = "login_type"
    const val REQUEST_API = "request_api"

    // Event
    const val LOGOUT = "logout"
    const val CALL = "call"

    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun init() {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = Firebase.analytics
        }
    }

    /**
     * @param userId
     */
    fun setUserId(userId: String) {
        firebaseAnalytics?.setUserId(userId)
    }

    /**
     * @param name
     * @param value
     */
    fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics?.setUserProperty(name, value)
    }

    /**
     * @param eventName
     * @param params
     */
    fun logEvent(eventName: String, params: Bundle?) {
        firebaseAnalytics?.logEvent(eventName, params)
    }
}