package com.moneyweather.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.moneyweather.base.BaseApplication
import com.moneyweather.data.remote.response.LockScreenResponse
import com.moneyweather.data.remote.response.LockScreenResponse.LockLandingEventInfo
import com.moneyweather.data.remote.response.WeatherResponse
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class PrefRepository private constructor(context: Context) {

    private val prefsFileName = "CgvPlus_Pref"

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(prefsFileName, Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var instance: PrefRepository? = null

        fun getInstance(): PrefRepository = instance ?: synchronized(this) {
            instance ?: PrefRepository(BaseApplication.appContext()).also { instance = it }
        }

        // JSON -> Map 변환
        fun parseJsonToMap(json: String): Map<String, Int> {
            return try {
                val jsonObject = JSONObject(json)
                val map = mutableMapOf<String, Int>()
                jsonObject.keys().forEach { key ->
                    map[key] = jsonObject.getInt(key)
                }
                map
            } catch (e: Exception) {
                emptyMap()
            }
        }

        // Map -> JSON 변환
        fun Map<String, Int>.toJson(): String {
            return JSONObject(this).toString()
        }

        // 키(yyyyMMdd 또는 yyyyMMddHH)를 밀리초로 변환
        fun parseKeyToMillis(key: String): Long? {
            return try {
                val format = if (key.length == 8) "yyyyMMdd" else "yyyyMMddHH"
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                sdf.parse(key)?.time
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun getInt(name: String, default: Int = 0): Int = sharedPreferences.getInt(name, default)
    private fun putInt(name: String, value: Int): Unit = sharedPreferences.edit().putInt(name, value).apply()

    private fun putInts(map: HashMap<String, Int>, default: Int = 0): Unit {
        val editor = sharedPreferences.edit()
        for (key in map.keys) {
            editor.putInt(key, map[key] ?: default)
        }
        editor.apply()
    }

    private fun getLong(name: String, default: Long = 0L): Long = sharedPreferences.getLong(name, default)
    private fun putLong(name: String, value: Long): Unit = sharedPreferences.edit().putLong(name, value).apply()

    private fun getFloat(name: String, default: Float = 0F): Float = sharedPreferences.getFloat(name, default)
    private fun putFloat(name: String, value: Float): Unit = sharedPreferences.edit().putFloat(name, value).apply()

    private fun getString(name: String, default: String = ""): String = sharedPreferences.getString(name, default) ?: ""
    private fun putString(name: String, value: String): Unit = sharedPreferences.edit().putString(name, value).apply()

    private fun getBoolean(name: String, default: Boolean = false): Boolean = sharedPreferences.getBoolean(name, default)
    private fun putBoolean(name: String, value: Boolean): Unit = sharedPreferences.edit().putBoolean(name, value).apply()

    object UserInfo {
        private const val ACCESS_TOKEN = "user-info:access-token"
        var accessToken: String
            get() {

                return getInstance().getString(ACCESS_TOKEN, "")
            }
            set(value) {
                getInstance().putString(ACCESS_TOKEN, value)
            }

        private const val REFRESH_TOKEN = "user-info:refresh-token"
        var refreshToken: String
            get() {
                return getInstance().getString(REFRESH_TOKEN, "")
            }
            set(value) {
                getInstance().putString(REFRESH_TOKEN, value)
            }

        private const val X_ACCESS_TOKEN = "user-info:x-access-token"
        var xAccessToken: String
            get() {
                return getInstance().getString(X_ACCESS_TOKEN, "")
            }
            set(value) {
                getInstance().putString(X_ACCESS_TOKEN, value)
            }

        private const val X_REFRESH_TOKEN = "user-info:x-refresh-token"
        var xRefreshToken: String
            get() {
                return getInstance().getString(X_REFRESH_TOKEN, "")
            }
            set(value) {
                getInstance().putString(X_REFRESH_TOKEN, value)
            }

        private const val FCM_TOKEN = "user-info:fcm-token"
        var fcmToken: String
            get() {
                return getInstance().getString(FCM_TOKEN, "")
            }
            set(value) {
                getInstance().putString(FCM_TOKEN, value)
            }

        private const val ADID = "user-info:adid"
        var adid: String
            get() {
                return getInstance().getString(ADID, "")
            }
            set(value) {
                getInstance().putString(ADID, value)
            }

        private const val USER_ID = "user-info:user_id"
        var userId: String
            get() {
                return getInstance().getString(USER_ID, "")
            }
            set(value) {
                getInstance().putString(USER_ID, value)
            }

        private const val EMAIL = "user-info:email"
        var email: String
            get() {
                return getInstance().getString(EMAIL, "")
            }
            set(value) {
                getInstance().putString(EMAIL, value)
            }

        private const val LOGIN_EMAIL = "user-info:login_email"
        var loginEmail: String
            get() {
                return getInstance().getString(LOGIN_EMAIL, "")
            }
            set(value) {
                getInstance().putString(LOGIN_EMAIL, value)
            }

        private const val NAME = "user-info:name"
        var name: String
            get() {
                return getInstance().getString(NAME, "")
            }
            set(value) {
                getInstance().putString(NAME, value)
            }

        private const val NICK_NAME = "user-info:nick-name"
        var nickName: String
            get() {
                return getInstance().getString(NICK_NAME, "")
            }
            set(value) {
                getInstance().putString(NICK_NAME, value)
            }

        private const val PROFILE = "user-info:profile"
        var profile: String
            get() {
                return getInstance().getString(PROFILE, "")
            }
            set(value) {
                getInstance().putString(PROFILE, value)
            }

        private const val PHONE = "user-info:phone"
        var phone: String
            get() {
                return getInstance().getString(PHONE, "")
            }
            set(value) {
                getInstance().putString(PHONE, value)
            }

        private const val CI = "user-info:ci"
        var ci: String
            get() {
                return getInstance().getString(CI, "")
            }
            set(value) {
                getInstance().putString(CI, value)
            }

        private const val AUTH_PHONE = "user-info:AUTHPHONE" //본인인증 폰번호
        var auth_phone: String
            get() {
                return getInstance().getString(AUTH_PHONE, "")
            }
            set(value) {
                getInstance().putString(AUTH_PHONE, value)
            }

        private const val AUTH = "user-info:AUTH" //본인인증 여부
        var auth: String
            get() {
                return getInstance().getString(AUTH, "")
            }
            set(value) {
                getInstance().putString(AUTH, value)
            }

        private const val IS_GUEST_LOGIN = "user-info:is-guest-login"
        var isGuestLogin: Boolean
            get() {
                return getInstance().getBoolean(IS_GUEST_LOGIN, false)
            }
            set(value) {
                getInstance().putBoolean(IS_GUEST_LOGIN, value)
            }

        private const val IS_LOGIN = "user-info:is-login"
        var isLogin: Boolean
            get() {
                return getInstance().getBoolean(IS_LOGIN, false)
            }
            set(value) {
                getInstance().putBoolean(IS_LOGIN, value)
            }

        private const val IS_FCM_TOKEN_UPDATED = "user-info:is-fcm-token-updated"
        var isFcmTokenUpdated: Boolean
            get() {
                return getInstance().getBoolean(IS_FCM_TOKEN_UPDATED, false)
            }
            set(value) {
                getInstance().putBoolean(IS_FCM_TOKEN_UPDATED, value)
            }

        private const val LAST_SAVED_TIME_MILLIS = "user-info:last-saved-time-millis"
        var lastSavedTimeMillis: Long
            get() {
                return getInstance().getLong(LAST_SAVED_TIME_MILLIS, 0)
            }
            set(value) {
                getInstance().putLong(LAST_SAVED_TIME_MILLIS, value)
            }

        private const val IS_VERIFICATION = "user-info:is-verification"
        var isVerification: Boolean
            get() {
                return getInstance().getBoolean(IS_VERIFICATION, false)
            }
            set(value) {
                getInstance().putBoolean(IS_VERIFICATION, value)
            }

        private const val IS_FIRST_RUN = "user-info:is-firstRun"
        var isFirstRun: Boolean
            get() {
                return getInstance().getBoolean(IS_FIRST_RUN, true)
            }
            set(value) {
                getInstance().putBoolean(IS_FIRST_RUN, value)
            }

        private const val IS_FIRST = "user-info:is-first"
        var isFirst: Boolean
            get() {
                return getInstance().getBoolean(IS_FIRST, true)
            }
            set(value) {
                getInstance().putBoolean(IS_FIRST, value)
            }

        private const val SAVE_USER_EMAIL = "user-info:saveEmail"
        var saveEmail: String
            get() {
                return getInstance().getString(SAVE_USER_EMAIL, "")
            }
            set(value) {
                getInstance().putString(SAVE_USER_EMAIL, value)
            }

        private const val SAVE_ID = "user-info:saveId"
        var saveId: Boolean
            get() {
                return getInstance().getBoolean(SAVE_ID, false)
            }
            set(value) {
                getInstance().putBoolean(SAVE_ID, value)
            }

        private const val SAVE_USER_PASSWORD = "user-info:savePasword"
        var savePassword: String
            get() {
                return getInstance().getString(SAVE_USER_PASSWORD, "")
            }
            set(value) {
                getInstance().putString(SAVE_USER_PASSWORD, value)
            }

        private const val SERVICE_TYPE = "user-info:service-type"
        var serviceType: String
            get() {
                return getInstance().getString(SERVICE_TYPE, "")
            }
            set(value) {
                getInstance().putString(SERVICE_TYPE, value)
            }

        private const val INVITE_RECOMMEND_CODE = "user-info:invite-recommend-code"
        var inviteRecommendCode: String
            get() {
                return getInstance().getString(INVITE_RECOMMEND_CODE, "")
            }
            set(value) {
                getInstance().putString(INVITE_RECOMMEND_CODE, value)
            }

        private const val GRADE = "user-info:grade"
        var grade: String
            get() {
                return getInstance().getString(GRADE, "")
            }
            set(value) {
                getInstance().putString(GRADE, value)
            }

        private const val USER_KEY = "user-info:user-key"
        var userKey: String
            get() {
                return getInstance().getString(USER_KEY, "")
            }
            set(value) {
                getInstance().putString(USER_KEY, value)
            }

        private const val IS_UNREGISTER = "user-info:is-unregister"
        var isUnregister: Boolean
            get() {
                return getInstance().getBoolean(IS_UNREGISTER, false)
            }
            set(value) {
                getInstance().putBoolean(IS_UNREGISTER, value)
            }

        private const val POINT = "user-info:point"
        var point: Int
            get() {
                return getInstance().getInt(POINT, 0)
            }
            set(value) {
                getInstance().putInt(POINT, value)
            }

        private const val BIRTHDAY = "user-info:birthday"
        var birthday: String
            get() {
                return getInstance().getString(BIRTHDAY, "")
            }
            set(value) {
                getInstance().putString(BIRTHDAY, value)
            }

        private const val SEX_CODE = "user-info:sex-code"
        var sexCode: String
            get() {
                return getInstance().getString(SEX_CODE, "")
            }
            set(value) {
                getInstance().putString(SEX_CODE, value)
            }

        private const val SNS_ID = "user-info:sns-id"
        var snsId: String
            get() {
                return getInstance().getString(SNS_ID, "")
            }
            set(value) {
                getInstance().putString(SNS_ID, value)
            }

        private const val HISTORY_ID = "user-info:history-id"
        var historyId: Int
            get() {
                return getInstance().getInt(HISTORY_ID, 0)
            }
            set(value) {
                getInstance().putInt(HISTORY_ID, value)
            }

        private const val MOBON_SC = "user-info:mobon-sc"
        var sc: String
            get() {
                return getInstance().getString(MOBON_SC, "")
            }
            set(value) {
                getInstance().putString(MOBON_SC, value)
            }

        private const val SERVICE_PUSH_AGREED = "user-info:service-push-agreed"
        var servicePushAgreed: Boolean
            get() {
                return getInstance().getBoolean(SERVICE_PUSH_AGREED, true)
            }
            set(value) {
                getInstance().putBoolean(SERVICE_PUSH_AGREED, value)
            }

        private const val MARKETING_PUSH_AGREED = "user-info:marketing-push-agreed"
        var marketingPushAgreed: Boolean
            get() {
                return getInstance().getBoolean(MARKETING_PUSH_AGREED, false)
            }
            set(value) {
                getInstance().putBoolean(MARKETING_PUSH_AGREED, value)
            }

        private const val NIGHT_PUSH_ALLOWED = "user-info:night-push-allowed"
        var nightPushAllowed: Boolean
            get() {
                return getInstance().getBoolean(NIGHT_PUSH_ALLOWED, true)
            }
            set(value) {
                getInstance().putBoolean(NIGHT_PUSH_ALLOWED, value)
            }

        fun clear() {
            val keys = arrayOf(
                ACCESS_TOKEN,
                REFRESH_TOKEN,
                // FCM_TOKEN,
                EMAIL,
                NAME,
                PROFILE,
                PHONE,
                IS_LOGIN,
                SERVICE_TYPE,
                INVITE_RECOMMEND_CODE,
                GRADE,
                USER_KEY,
                IS_UNREGISTER,
                POINT,
                BIRTHDAY,
                SEX_CODE,
                NICK_NAME,
                SNS_ID
            )

            keys.forEach { element ->
                getInstance().sharedPreferences.edit().remove(element).apply()
            }
        }

        fun clearInviteRecommendCode() {
            getInstance().sharedPreferences.edit().remove(INVITE_RECOMMEND_CODE).apply()
        }

        fun clearFcmToken() {
            getInstance().sharedPreferences.edit().remove(FCM_TOKEN).apply()
        }

    }

    object SettingInfo {
        private const val BATTERY_OPTIMIZATION_SAVE_TIME = "setting-info:battery-optimization-save-time"
        var batteryOptimizationSaveTime: Long
            get() {
                return getInstance().getLong(BATTERY_OPTIMIZATION_SAVE_TIME, 0)
            }
            set(value) {
                getInstance().putLong(BATTERY_OPTIMIZATION_SAVE_TIME, value)
            }

        private const val USE_LOCK_SCREEN = "setting-info:use-lock-screen"
        var useLockScreen: Boolean
            get() {
                return getInstance().getBoolean(USE_LOCK_SCREEN, true)
            }
            set(value) {
                getInstance().putBoolean(USE_LOCK_SCREEN, value)
            }

        private const val USE_SAVE_POINT_SOUND = "setting-info:use-save-point-sound"
        var useSavePointSound: Boolean
            get() {
                return getInstance().getBoolean(USE_SAVE_POINT_SOUND, true)
            }
            set(value) {
                getInstance().putBoolean(USE_SAVE_POINT_SOUND, value)
            }

        private const val USE_SAVE_POINT_VIBRATION = "setting-info:use-save-point-vibration"
        var useSavePointVibration: Boolean
            get() {
                return getInstance().getBoolean(USE_SAVE_POINT_VIBRATION, true)
            }
            set(value) {
                getInstance().putBoolean(USE_SAVE_POINT_VIBRATION, value)
            }

        private const val ALLOW_PUSH = "setting-info:allow-push"
        var allowPush: Boolean
            get() {
                return getInstance().getBoolean(ALLOW_PUSH, true)
            }
            set(value) {
                getInstance().putBoolean(ALLOW_PUSH, value)
            }

        private const val SERVICE_MODE = "setting-info:service-mode"
        var serviceMode: String
            get() {

                return getInstance().getString(SERVICE_MODE, "")
            }
            set(value) {
                getInstance().putString(SERVICE_MODE, value)
            }


        private const val SERVICE_REAL_STOP = "setting-info:service-real-stop"
        var serviceRealStop: Boolean
            get() {
                return getInstance().getBoolean(SERVICE_REAL_STOP, false)
            }
            set(value) {
                getInstance().putBoolean(SERVICE_REAL_STOP, value)
            }

        private const val LOCKSCREEN_LIMIT_TIME = "setting-info:lockscreen-limit-time"
        var lockscreenLimitTime: Long
            get() {
                return getInstance().getLong(LOCKSCREEN_LIMIT_TIME, 0)
            }
            set(value) {
                getInstance().getLong(LOCKSCREEN_LIMIT_TIME, value)
            }

        private const val SELECT_THEME_TYPE = "setting-info:select-theme-type"
        var selectThemeType: Int
            get() {
                return getInstance().getInt(SELECT_THEME_TYPE, 0)
            }
            set(value) {
                getInstance().putInt(SELECT_THEME_TYPE, value)
            }

        private const val VERIFICATION_URL = "setting-info:verification-url"
        var verificationUrl: String
            get() {
                return getInstance().getString(VERIFICATION_URL)
            }
            set(value) {
                getInstance().putString(VERIFICATION_URL, value)
            }

        private const val TERMS_URL = "setting-info:terms-url"
        var termsUrl: String
            get() {
                return getInstance().getString(TERMS_URL)
            }
            set(value) {
                getInstance().putString(TERMS_URL, value)
            }

        private const val PRIVACY_URL = "setting-info:privacy-url"
        var privacyUrl: String
            get() {
                return getInstance().getString(PRIVACY_URL)
            }
            set(value) {
                getInstance().putString(PRIVACY_URL, value)
            }

        private const val LOCATION_URL = "setting-info:location-url"
        var locationUrl: String
            get() {
                return getInstance().getString(LOCATION_URL)
            }
            set(value) {
                getInstance().putString(LOCATION_URL, value)
            }

        private const val COMPANY_URL = "setting-info:company-url"
        var companyUrl: String
            get() {
                return getInstance().getString(COMPANY_URL)
            }
            set(value) {
                getInstance().putString(COMPANY_URL, value)
            }

        private const val INVITE_BANNER_IMAGE_URL = "setting-info:invite-image-url"
        var inviteBannerImageUrl: String
            get() {
                return getInstance().getString(INVITE_BANNER_IMAGE_URL)
            }
            set(value) {
                getInstance().putString(INVITE_BANNER_IMAGE_URL, value)
            }

        private const val EXTERNAL_WEATHER_URL = "setting-info:external-weather-url"
        var externalWeatherUrl: String
            get() {
                return getInstance().getString(EXTERNAL_WEATHER_URL)
            }
            set(value) {
                getInstance().putString(EXTERNAL_WEATHER_URL, value)
            }

        private const val EXTERNAL_WEATHER_SEARCH_URL = "setting-info:external-weather-search-url"
        var externalWeatherSearchUrl: String
            get() {
                return getInstance().getString(EXTERNAL_WEATHER_SEARCH_URL)
            }
            set(value) {
                getInstance().putString(EXTERNAL_WEATHER_SEARCH_URL, value)
            }

        private const val CHECK_PERMISSION = "setting-info:check-permission"
        var checkPermission: Long
            get() {
                return getInstance().getLong(CHECK_PERMISSION, 0)
            }
            set(value) {
                getInstance().putLong(CHECK_PERMISSION, value)
            }

        private const val CHECK_NOTICE_POPUP = "setting-info:check-notice-popup"
        var checkNoticePopup: String
            get() {
                return getInstance().getString(CHECK_NOTICE_POPUP, "")
            }
            set(value) {
                getInstance().putString(CHECK_NOTICE_POPUP, value)
            }

        private const val CHECK_NEW_INFO_POPUP = "setting-info:check-new-info-popup"
        var checkNewInfoPopup: Boolean
            get() {
                return getInstance().getBoolean(CHECK_NEW_INFO_POPUP, false)
            }
            set(value) {
                getInstance().putBoolean(CHECK_NEW_INFO_POPUP, value)
            }

        private const val CHECK_NEW_INFO_POPUP_TODAY = "setting-info:check-new-info-popup-today"
        var checkNewInfoPopupToday: String
            get() {
                return getInstance().getString(CHECK_NEW_INFO_POPUP_TODAY, "")
            }
            set(value) {
                getInstance().putString(CHECK_NEW_INFO_POPUP_TODAY, value)
            }

        private const val CHECK_UPDATE_POPUP = "setting-info:check-update-popup"
        var checkUpdatePopup: String
            get() {
                return getInstance().getString(CHECK_UPDATE_POPUP, "")
            }
            set(value) {
                getInstance().putString(CHECK_UPDATE_POPUP, value)
            }

        fun clear() {
            val keys = arrayOf(BATTERY_OPTIMIZATION_SAVE_TIME, USE_LOCK_SCREEN, LOCKSCREEN_LIMIT_TIME)

            keys.forEach { element ->
                getInstance().sharedPreferences.edit().remove(element).apply()
            }
        }

        fun clearUseLockScreen() {
            getInstance().sharedPreferences.edit().remove(USE_LOCK_SCREEN).apply()
            getInstance().putBoolean(USE_LOCK_SCREEN, true)
        }
    }

    object LocationInfo {
        private const val LATITUDE = "latitude"
        var latitude: String
            get() {
                return getInstance().getString(LATITUDE, "37.49265")
            }
            set(value) {
                getInstance().putString(LATITUDE, value)
            }

        private const val LONGITUDE = "longitude"
        var longitude: String
            get() {
                return getInstance().getString(LONGITUDE, "126.8895972")
            }
            set(value) {
                getInstance().putString(LONGITUDE, value)
            }
    }

    object LockQuickInfo {
        private const val FIRST_IMAGE = "lock-quick-info:first-image"
        var firstImage: String
            get() {
                return getInstance().getString(FIRST_IMAGE)
            }
            set(value) {
                getInstance().putString(FIRST_IMAGE, value)
            }

        private const val FIRST_NAME = "lock-quick-info:first-name"
        var firstName: String
            get() {
                return getInstance().getString(FIRST_NAME)
            }
            set(value) {
                getInstance().putString(FIRST_NAME, value)
            }

        private const val SECOND_IMAGE = "lock-quick-info:second-image"
        var secondImage: String
            get() {
                return getInstance().getString(SECOND_IMAGE)
            }
            set(value) {
                getInstance().putString(SECOND_IMAGE, value)
            }

        private const val SECOND_NAME = "lock-quick-info:second-name"
        var secondName: String
            get() {
                return getInstance().getString(SECOND_NAME)
            }
            set(value) {
                getInstance().putString(SECOND_NAME, value)
            }

        private const val THIRD_IMAGE = "lock-quick-info:third-image"
        var thirdImage: String
            get() {
                return getInstance().getString(THIRD_IMAGE)
            }
            set(value) {
                getInstance().putString(THIRD_IMAGE, value)
            }

        private const val THIRD_NAME = "lock-quick-info:third-name"
        var thirdName: String
            get() {
                return getInstance().getString(THIRD_NAME)
            }
            set(value) {
                getInstance().putString(THIRD_NAME, value)
            }

        private const val KEY_END_POPUP_DATE = "key_end_popup_date"
        var mainPopupDate: String
            get() {
                return getInstance().getString(KEY_END_POPUP_DATE, "")
            }
            set(value) {
                getInstance().putString(KEY_END_POPUP_DATE, value)
            }

        private const val KEY_END_TRACKING_CARD = "key_end_tracking_card"
        var trackingCardCount: Int
            get() {
                return getInstance().getInt(KEY_END_TRACKING_CARD, 0)
            }
            set(value) {
                getInstance().putInt(KEY_END_TRACKING_CARD, value)
            }

        private const val KEY_ROCK_CARD_DATA = "key_rock_card_data"
        var rockCardData: String
            get() {
                return getInstance().getString(KEY_ROCK_CARD_DATA, "")
            }
            set(value) {
                getInstance().putString(KEY_ROCK_CARD_DATA, value)
            }

        private const val KEY_ROCK_SCREEN_TIME_CLEAR = "key_rock_screen_time_clear"
        var rockScreenTimeClear: String
            get() {
                return getInstance().getString(KEY_ROCK_SCREEN_TIME_CLEAR, "")
            }
            set(value) {
                getInstance().putString(KEY_ROCK_SCREEN_TIME_CLEAR, value)
            }

        private const val KEY_ROCK_SCREEN_TIME_CHECK = "key_rock_screen_time_check"
        var rockScreenTimeCheck: Long
            get() {
                return getInstance().getLong(KEY_ROCK_SCREEN_TIME_CHECK, 0)
            }
            set(value) {
                getInstance().putLong(KEY_ROCK_SCREEN_TIME_CHECK, value)
            }

        private const val KEY_ROCK_SCREEN_TIME_CHECK2 = "key_rock_screen_time_check2"
        var rockScreenTimeCheck2: String
            get() {
                return getInstance().getString(KEY_ROCK_SCREEN_TIME_CHECK2, "")
            }
            set(value) {
                getInstance().putString(KEY_ROCK_SCREEN_TIME_CHECK2, value)
            }

        private const val KEY_ROCK_SCREEN_TIME_OFFCODE = "KEY_ROCK_SCREEN_TIME_OFFCODE"
        var rockScreenTimeOffCode: Int
            get() {
                return getInstance().getInt(KEY_ROCK_SCREEN_TIME_OFFCODE, -1)
            }
            set(value) {
                getInstance().putInt(KEY_ROCK_SCREEN_TIME_OFFCODE, value)
            }

        private const val WARNING_TITLE = "lock-quick-info:warning-title"
        var warningTitle: String
            get() {
                return getInstance().getString(WARNING_TITLE)
            }
            set(value) {
                getInstance().putString(WARNING_TITLE, value)
            }

        private const val NOTICE_ID = "lock-quick-info:notice-id"
        var noticeId: Int
            get() {
                return getInstance().getInt(NOTICE_ID)
            }
            set(value) {
                getInstance().putInt(NOTICE_ID, value)
            }

        private const val IS_CHANGE_WARNING_TITLE = "lock-quick-info:is-change-warning-title"
        var isChangeWarningTitle: Boolean
            get() {
                return getInstance().getBoolean(IS_CHANGE_WARNING_TITLE, false)
            }
            set(value) {
                getInstance().putBoolean(IS_CHANGE_WARNING_TITLE, value)
            }

        private const val IS_CLOSE_WARNING_NEWS = "lock-quick-info:is-close-warning-news"
        var isCloseWarningNews: Boolean
            get() {
                return getInstance().getBoolean(IS_CLOSE_WARNING_NEWS, false)
            }
            set(value) {
                getInstance().putBoolean(IS_CLOSE_WARNING_NEWS, value)
            }

        private const val IS_THEME_CHANGE = "lock-quick-info:is-theme-change"
        var isThemeChange: Boolean
            get() {
                return getInstance().getBoolean(IS_THEME_CHANGE, false)
            }
            set(value) {
                getInstance().putBoolean(IS_THEME_CHANGE, value)
            }

        private const val INFO_THEME_DATA = "lock-quick-info:info-theme-data"
        var infoTheme: LockScreenResponse?
            get() {
                return getInstance().getString(INFO_THEME_DATA, "").let { data ->
                    if (data.isEmpty()) {
                        null
                    } else {
                        Gson().fromJson(data, LockScreenResponse::class.java)
                    }
                }
            }
            set(value) {
                getInstance().putString(INFO_THEME_DATA, Gson().toJson(value))
            }

        private const val SIMPLE_THEME_DATA = "lock-quick-info:simple-theme-data"
        var simpleTheme: LockScreenResponse?
            get() {
                return getInstance().getString(SIMPLE_THEME_DATA, "").let { data ->
                    if (data.isEmpty()) {
                        null
                    } else {
                        Gson().fromJson(data, LockScreenResponse::class.java)
                    }
                }
            }
            set(value) {
                getInstance().putString(SIMPLE_THEME_DATA, Gson().toJson(value))
            }

        private const val NEWS_FEED_DATA = "lock-quick-info:news-feed-data"
        var newsFeedData: LockScreenResponse.Newsfeed?
            get() {
                return getInstance().getString(NEWS_FEED_DATA, "").let { data ->
                    if (data.isEmpty()) {
                        null
                    } else {
                        Gson().fromJson(data, LockScreenResponse.Newsfeed::class.java)
                    }
                }
            }
            set(value) {
                getInstance().putString(NEWS_FEED_DATA, Gson().toJson(value))
            }

        private const val LOCK_LANDING_EVENT_INFO = "lock-quick-info:lock-landing-event-info"
        var lockLandingEventInfo: LockLandingEventInfo?
            get() {
                return getInstance().getString(LOCK_LANDING_EVENT_INFO, "").let { data ->
                    if (data.isEmpty()) {
                        null
                    } else {
                        Gson().fromJson(data, LockLandingEventInfo::class.java)
                    }
                }
            }
            set(value) {
                getInstance().putString(LOCK_LANDING_EVENT_INFO, Gson().toJson(value))
            }

        private const val CURRENT_USER_POINT = "lock-quick-info:current-user-point"
        var currentUserPoint: Int
            get() {
                return getInstance().getInt(CURRENT_USER_POINT, 0)
            }
            set(value) {
                getInstance().putInt(CURRENT_USER_POINT, value)
            }

        private const val TODAY_AVAILABLE_POINT = "lock-quick-info:today-available-point"
        var todayAvailablePoint: Int
            get() {
                return getInstance().getInt(TODAY_AVAILABLE_POINT, 0)
            }
            set(value) {
                getInstance().putInt(TODAY_AVAILABLE_POINT, value)
            }

        private const val CHECK_BATTERY_OPTIMIZATIONS = "lock-quick-info:check-battery-optimizations"
        var checkBatteryOptimizations: Long
            get() {
                return getInstance().getLong(CHECK_BATTERY_OPTIMIZATIONS, 0)
            }
            set(value) {
                getInstance().putLong(CHECK_BATTERY_OPTIMIZATIONS, value)
            }

        private const val NOTI_WEATHER_DATA = "lock-quick-info:noti-weather-data"
        var notiWeather: WeatherResponse
            get() {
                return getInstance().getString(NOTI_WEATHER_DATA, "").let { data ->
                    if (data.isEmpty()) {
                        WeatherResponse.EMPTY
                    } else {
                        Gson().fromJson(data, WeatherResponse::class.java)
                    }
                }
            }
            set(value) {
                getInstance().putString(NOTI_WEATHER_DATA, Gson().toJson(value))
            }

        private const val SHOW_WEATHER_FORECAST = "lock-quick-info:show-weather-forecast"
        var showWeatherForecast: Boolean
            get() {
                return getInstance().getBoolean(SHOW_WEATHER_FORECAST, true)
            }
            set(value) {
                getInstance().putBoolean(SHOW_WEATHER_FORECAST, value)
            }

        private const val IS_LOCKSCREEN_FIRST = "lock-quick-info:is-lockscreen-first"
        var isLockScreenFirst: Boolean
            get() {
                return getInstance().getBoolean(IS_LOCKSCREEN_FIRST, true)
            }
            set(value) {
                getInstance().putBoolean(IS_LOCKSCREEN_FIRST, value)
            }

        private const val DAILY_NEWSFEED_REWARD_COUNT = "lock-quick-info:daily-newsfeed-reward-count"
        var dailyNewsfeedRewardCount: Int
            get() {
                val dataJson = getInstance().getString(DAILY_NEWSFEED_REWARD_COUNT, "{}")
                val dataMap = parseJsonToMap(dataJson ?: "{}")
                return dataMap[CommonUtils.getCurrentDate()] ?: 0
            }
            set(value) {
                val dataJson = getInstance().getString(DAILY_NEWSFEED_REWARD_COUNT, "{}")
                val dataMap = parseJsonToMap(dataJson ?: "{}").toMutableMap()

                // 오래된 데이터 정리: 3일 이상된 데이터 제거
                val now = System.currentTimeMillis()
                val sevenDaysAgo = now - (3 * 24 * 60 * 60 * 1000)
                dataMap.entries.removeIf { entry ->
                    val keyDate = parseKeyToMillis(entry.key)
                    keyDate != null && keyDate < sevenDaysAgo
                }

                // 현재 시간 데이터 추가 또는 갱신 yyyyMMdd
                dataMap[CommonUtils.getCurrentDate()] = value

                // JSON으로 다시 저장
                getInstance().putString(DAILY_NEWSFEED_REWARD_COUNT, dataMap.toJson())
            }

        private const val HOURLY_NEWSFEED_REWARD_COUNT = "lock-quick-info:hourly-newsfeed-reward-count"
        var hourlyNewsfeedRewardCount: Int
            get() {
                val dataJson = getInstance().getString(HOURLY_NEWSFEED_REWARD_COUNT, "{}")
                val dataMap = parseJsonToMap(dataJson ?: "{}")
                return dataMap[CommonUtils.getCurrentDate2()] ?: 0
            }
            set(value) {
                val dataJson = getInstance().getString(HOURLY_NEWSFEED_REWARD_COUNT, "{}")
                val dataMap = parseJsonToMap(dataJson ?: "{}").toMutableMap()

                // 오래된 데이터 정리: 3일 이상된 데이터 제거
                val now = System.currentTimeMillis()
                val sevenDaysAgo = now - (3 * 24 * 60 * 60 * 1000)
                dataMap.entries.removeIf { entry ->
                    val keyDate = parseKeyToMillis(entry.key)
                    keyDate != null && keyDate < sevenDaysAgo
                }

                // 현재 시간 데이터 추가 또는 갱신 yyyyMMddhh
                dataMap[CommonUtils.getCurrentDate2()] = value

                // JSON으로 다시 저장
                getInstance().putString(HOURLY_NEWSFEED_REWARD_COUNT, dataMap.toJson())
            }

        private const val CHECK_DEFAULT_POINT_SAVE = "lock-quick-info:check-default-point-save"
        var checkDefaultPointSave: String
            get() {
                return getInstance().getString(CHECK_DEFAULT_POINT_SAVE, "")
            }
            set(value) {
                getInstance().putString(CHECK_DEFAULT_POINT_SAVE, value)
            }

        private const val LOCK_SCREEN_BOTTOM_BANNER_INDEX = "lock-quick-info:lock-screen-bottom-banner-index"
        var lockScreenBottomBannerIndex: Int
            get() {
                return getInstance().getInt(LOCK_SCREEN_BOTTOM_BANNER_INDEX, 0)
            }
            set(value) {
                getInstance().putInt(LOCK_SCREEN_BOTTOM_BANNER_INDEX, value)
            }

        private const val IS_CHANGE_REMOTE_CONFIG = "lock-quick-info:is-change-remote-config"
        var isChangeRemoteConfig: Boolean
            get() {
                return getInstance().getBoolean(IS_CHANGE_REMOTE_CONFIG, false)
            }
            set(value) {
                getInstance().putBoolean(IS_CHANGE_REMOTE_CONFIG, value)
            }

        private const val BANNER_EXPOSURE_COUNT = "lock-quick-info:banner-exposure-count"
        var bannerExposureCount: Int
            get() {
                return getInstance().getInt(BANNER_EXPOSURE_COUNT, 20)
            }
            set(value) {
                getInstance().putInt(BANNER_EXPOSURE_COUNT, value)
            }

        private const val BANNER_EXPOSURE_RATIO = "lock-quick-info:banner-exposure-ratio"
        var bannerExposureRatio: String
            get() {
                return getInstance().getString(BANNER_EXPOSURE_RATIO, "5:5")
            }
            set(value) {
                getInstance().putString(BANNER_EXPOSURE_RATIO, value)
            }

        private const val BANNER_EXPOSURE_ADPIE_ORDER = "lock-quick-info:banner-exposure-adpie_order"
        var bannerExposureAdPieOrder: String
            get() {
                return getInstance().getString(BANNER_EXPOSURE_ADPIE_ORDER, "native")
            }
            set(value) {
                getInstance().putString(BANNER_EXPOSURE_ADPIE_ORDER, value)
            }

        private const val CHARGE_BUZZVIL_AVAILABLE = "lock-quick-info:charge-buzzvil-available"
        var chargeBuzzvilAvailable: Boolean
            get() {
                return getInstance().getBoolean(CHARGE_BUZZVIL_AVAILABLE, true)
            }
            set(value) {
                getInstance().putBoolean(CHARGE_BUZZVIL_AVAILABLE, value)
            }

        private const val IS_SHOW_EARN_POINT_MESSAGE = "lock-quick-info:is-show-earn-point-message"
        var isShowEarnPointMessage: Boolean
            get() {
                return getInstance().getBoolean(IS_SHOW_EARN_POINT_MESSAGE, false)
            }
            set(value) {
                getInstance().putBoolean(IS_SHOW_EARN_POINT_MESSAGE, value)
            }

        private const val BOTTOM_BANNER_CLICKED_TIME = "lock-quick-info:bottom-banner-clicked-time"
        var bottomBannerClickedTime: Long
            get() {
                return getInstance().getLong(BOTTOM_BANNER_CLICKED_TIME, 0L)
            }
            set(value) {
                getInstance().putLong(BOTTOM_BANNER_CLICKED_TIME, value)
            }

        private const val LOCK_SCREEN_COUPANG_CPS_CLICKED = "lock-quick-info:lock-screen-coupang-cps-clicked"
        var lockScreenCoupangCpsClicked: Boolean
            get() {
                return getInstance().getBoolean(LOCK_SCREEN_COUPANG_CPS_CLICKED, false)
            }
            set(value) {
                getInstance().putBoolean(LOCK_SCREEN_COUPANG_CPS_CLICKED, value)
            }

        private const val POMISSION_ZONE_URL = "lock-quick-info:pomission-zone-url"
        var pomissionZoneUrl: String
            get() {
                return getInstance().getString(POMISSION_ZONE_URL, "")
            }
            set(value) {
                getInstance().putString(POMISSION_ZONE_URL, value)
            }

        private const val POMISSION_ZONE_ENTER_POINT_AVAILABLE = "lock-quick-info:pomission-zone-enter-point-available"
        var pomissionZoneEnterPointAvailable: Boolean
            get() {
                return getInstance().getBoolean(POMISSION_ZONE_ENTER_POINT_AVAILABLE, false)
            }
            set(value) {
                getInstance().putBoolean(POMISSION_ZONE_ENTER_POINT_AVAILABLE, value)
            }
    }
}