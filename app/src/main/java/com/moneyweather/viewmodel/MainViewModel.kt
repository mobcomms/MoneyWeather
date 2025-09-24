package com.moneyweather.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.enliple.banner.MobSDK
import com.enliple.banner.common.Listener
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseApplication
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.UrlHelper.SHOPLUS_DOMAIN
import com.moneyweather.data.remote.model.ApiAnicModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.data.remote.response.NoticeDetailResponse
import com.moneyweather.event.EventDelegate
import com.moneyweather.event.main.MainUiEvent
import com.moneyweather.extensions.landingPage
import com.moneyweather.extensions.openOfferwall
import com.moneyweather.extensions.retryWithBackoff
import com.moneyweather.extensions.toBooleanYN
import com.moneyweather.extensions.toYN
import com.moneyweather.model.AppInfo
import com.moneyweather.model.enums.OfferwallEnum
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.util.CustomToast
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.PrefRepository
import com.moneyweather.util.analytics.GaButtonClickEvent
import com.moneyweather.view.AppWebViewActivity
import com.moneyweather.view.LockScreenWebViewActivity.Companion.KEY_LOAD_URL
import com.moneyweather.view.MainActivity.Companion.getTnkOfferwall
import com.moneyweather.view.fragment.MainHomeFragment.Companion.SUCCESS_GAME
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val apiUserModel: ApiUserModel,
    val apiAnicModel: ApiAnicModel
) : BaseKotlinViewModel(), EventDelegate<Unit, MainUiEvent> by EventDelegate.EventDelegateImpl() {

    var resultNoticeDetail: MutableLiveData<NoticeDetailResponse.Data> = MutableLiveData()

    override fun dispatchEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.RefreshFcmToken -> {
                PrefRepository.UserInfo.fcmToken
                    .takeIf { it.isNotEmpty() }
                    .takeIf { !PrefRepository.UserInfo.isFcmTokenUpdated }
                    ?.let { token ->
                        updateFcmToken(token)
                    }
            }

            is MainUiEvent.OpenOfferwall -> {
                openOfferwallLandingPage(event.context, event.landingEventId)
            }

            is MainUiEvent.FetchPushAgree -> {
                getPushAgree()
            }

            is MainUiEvent.UpdatePushAgree -> {
                updatePushAgree(
                    servicePushAgreed = event.servicePushAgreed,
                    marketingPushAgreed = event.marketingPushAgreed,
                    nightPushAllowed = event.nightPushAllowed
                )
            }

            is MainUiEvent.FetchPomissionZoneUrl -> {
                fetchPomissionZoneUrl()
            }
        }
    }

    /**
     * 알림 설정 조회
     */
    private fun getPushAgree() {
        addDisposable(
            apiUserModel.getPushAgree()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    response.data?.let { data ->
                        PrefRepository.UserInfo.apply {
                            servicePushAgreed = data.servicePushAgreed.toBooleanYN()
                            marketingPushAgreed = data.marketingPushAgreed.toBooleanYN()
                            nightPushAllowed = data.nightPushAllowed.toBooleanYN()
                        }
                    }
                }, { throwable ->
                    throwable.printStackTrace()
                })
        )
    }

    private fun fetchPomissionZoneUrl() {
        addDisposable(
            apiUserModel.getPomissionZoneUrl()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWithBackoff()
                .subscribe({ response ->
                    PrefRepository.LockQuickInfo.pomissionZoneUrl = response.data.pomissionZoneUrl
                }, { throwable ->
                    throwable.printStackTrace()
                })

        )
    }

    /**
     * 알림 설정 변경
     * @param servicePushAgreed
     * @param marketingPushAgreed
     * @param nightPushAllowed
     */
    private fun updatePushAgree(
        servicePushAgreed: Boolean,
        marketingPushAgreed: Boolean,
        nightPushAllowed: Boolean
    ) {
        val params = mapOf(
            "servicePushAgreed" to servicePushAgreed.toYN(),
            "marketingPushAgreed" to marketingPushAgreed.toYN(),
            "nightPushAllowed" to nightPushAllowed.toYN()
        )

        apiUserModel.updatePushAgree(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                if (response.isSuccessful) {
                    PrefRepository.UserInfo.servicePushAgreed = servicePushAgreed
                    PrefRepository.UserInfo.marketingPushAgreed = marketingPushAgreed
                    PrefRepository.UserInfo.nightPushAllowed = nightPushAllowed
                }
            }, { throwable ->
                throwable.printStackTrace()
            })
    }

    /**
     * 오퍼월 랜딩 페이지로 이동
     * @param context
     * @param landingEventId
     */
    private fun openOfferwallLandingPage(context: Context, landingEventId: Int) {
        when (OfferwallEnum.getLandingPage(landingEventId)) {
            OfferwallEnum.TNK_VIDEO -> {
                getTnkOfferwall(context, PrefRepository.UserInfo.userId).landingPage(
                    context,
                    OfferwallEnum.TNK_VIDEO.category,
                    OfferwallEnum.TNK_VIDEO.filter
                )

                // Ga Log Event
                GaButtonClickEvent.logLockScreenOfferwallTnkVideoButtonClickEvent()
            }

            OfferwallEnum.TNK_QUIZ -> {
                getTnkOfferwall(context, PrefRepository.UserInfo.userId).landingPage(
                    context,
                    OfferwallEnum.TNK_QUIZ.category,
                    OfferwallEnum.TNK_QUIZ.filter
                )

                // Ga Log Event
                GaButtonClickEvent.logLockScreenOfferwallTnkQuizButtonClickEvent()
            }

            OfferwallEnum.TNK_NEWS -> {
                getTnkOfferwall(context, PrefRepository.UserInfo.userId).landingPage(
                    context,
                    OfferwallEnum.TNK_NEWS.category,
                    OfferwallEnum.TNK_NEWS.filter
                )

                // Ga Log Event
                GaButtonClickEvent.logLockScreenOfferwallTnkNewsButtonClickEvent()
            }

            else -> {
                getTnkOfferwall(context, PrefRepository.UserInfo.userId)
                    .openOfferwall(context)
            }
        }
    }

    /**
     * Main 화면 진입 후 (로그인 후) FCM token 업데이트
     *
     * @param token
     *
     */
    @SuppressLint("CheckResult")
    private fun updateFcmToken(token: String) {
        apiUserModel.updateFcmToken(token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                if (response.isSuccessful) {
                    PrefRepository.UserInfo.isFcmTokenUpdated = true
                }
            }, { throwable ->
                throwable.printStackTrace()
            })
    }


    fun connectConfigInit() {
        addDisposable(
            apiUserModel.configInit()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
//                    dismissIndicator()
                    PrefRepository.SettingInfo.verificationUrl = it.data.verificationUrl
                    PrefRepository.SettingInfo.termsUrl = it.data.termsOfServiceUrl
                    PrefRepository.SettingInfo.privacyUrl = it.data.termsOfPrivacyUrl
                    PrefRepository.SettingInfo.locationUrl = it.data.termsOfLocationUrl
                    PrefRepository.SettingInfo.companyUrl = it.data.companyUrl
                    PrefRepository.SettingInfo.inviteBannerImageUrl = it.data.inviteBannerImageUrl
                    PrefRepository.SettingInfo.externalWeatherUrl = it.data.externalWeatherUrl
                    PrefRepository.SettingInfo.externalWeatherSearchUrl = it.data.externalWeatherSearchUrl
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectConfigInit()
                                }
                            }

                            else -> Toast.makeText(
                                BaseApplication.appContext(),
                                it.errorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
        )
    }

    fun connectUserInfo() {
        addDisposable(
            apiUserModel.userInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
//                    dismissIndicator()
                    if (it.result == 0) {
                        AppInfo.setUserInfo(it.data)
                        PrefRepository.UserInfo.userId = it.data.userId
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectUserInfo()
                                }
                            }

                            else -> Toast.makeText(
                                BaseApplication.appContext(),
                                it.errorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
        )
    }

    fun connectUserPoint() {
        addDisposable(
            apiUserModel.userPoint()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
//                    dismissIndicator()
                    if (it.result == 0) {
                        AppInfo.setUserPoint(it.data)


                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectUserPoint()
                                }
                            }

                            else -> Toast.makeText(
                                BaseApplication.appContext(),
                                it.errorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
        )
    }

    fun getCurrentWeather(lat: String, lon: String) {
        addDisposable(
            apiUserModel.currentWeather(lat, lon)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {
                        dismissIndicator()

                        AppInfo.setCurrentWeather(it.getWeather())
                        AppInfo.setRegion(it.getRegion())
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    getCurrentWeather(lat, lon)
                                }
                            }

                            else -> Toast.makeText(
                                BaseApplication.appContext(),
                                it.errorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
        )
    }

    /**
     * 공지 상세 조회
     * @param noticeId
     */
    fun connectNoticeDetail(noticeId: Int) {
        addDisposable(
            apiUserModel.noticeDetail(noticeId)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.run { resultNoticeDetail.value = it.data }
                }, {
                    throwableCheck(it)
                })
        )
    }

    /**
     * @param context
     */
    fun startShoplus(context: Context) {
        try {
            val baseUrl = SHOPLUS_DOMAIN
            val userId = PrefRepository.UserInfo.userId
            val adId = PrefRepository.UserInfo.adid
            val affliateId = getString(R.string.shop_plus_affliate_id)
            val site = getString(R.string.shop_plus_site)
            val zoneId = getString(R.string.shop_plus_zone_id)

            val shopPlusUrl = StringBuilder()
                .append(baseUrl)
                .append("?userId=${userId}")
                .append("&adId=${adId}")
                .append("&affliateId=${affliateId}")
                .append("&site=${site}")
                .append("&zoneId=${zoneId}")

            val intent = Intent(context, AppWebViewActivity::class.java)
            intent.putExtra(KEY_LOAD_URL, shopPlusUrl.toString())
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startAnicGame() {
        connectAnicParticipationCheck(true)
    }

    /**
     * 에이닉 게임 참여 1차 신청
     * @param b true:홈에서 실행, false:게임 존에서 연속 참여인 경우
     */
    private fun connectAnicParticipationCheck(b: Boolean) {
        addDisposable(
            apiUserModel.anic()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    PrefRepository.UserInfo.historyId = it.data.historyId
                    if (b) initAnic()
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (body.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectAnicParticipationCheck(b)
                                }
                            }

                            else -> {
                                CustomToast.showToast(getContext(), body.errorMessage!!)
                            }
                        }
                    }
                })
        )
    }

    /**
     * 에이닉 포인트 적립 신청
     * @param point
     * @param trackingId
     * @param type
     * @param location
     */
    private fun connectAnicPoint(point: Int, trackingId: Int, type: String, location: String) {
        var m = HashMap<String, Any?>()
        m["point"] = point
        m["trackingId"] = trackingId
        m["type"] = type
        m["location"] = location

        addDisposable(
            apiUserModel.anic(m)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    connectAnicParticipationCheck(false)
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (body.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectAnicPoint(point, trackingId, type, location)
                                }
                            }

                            else -> {
                                connectAnicParticipationCheck(false)
                                connectAnicGameFail(trackingId)
                            }
                        }
                    }
                })
        )
    }

    /**
     * @param trackingId
     */
    private fun connectAnicGameFail(trackingId: Int) {
        var m = HashMap<String, Any?>()
        m["tracking_id"] = trackingId
        m["fail_id"] = PrefRepository.UserInfo.userId.toInt()

        addDisposable(
            apiAnicModel.anicGameFail(m)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                }, {

                })
        )
    }

    private fun initAnic() {
        var userId = PrefRepository.UserInfo.userId
        android.os.Handler(Looper.getMainLooper()).post(Runnable {
            MobSDK.setUserId(userId) { isSetUserSuccess: Boolean ->
                if (isSetUserSuccess) {
                    try {
                        MobSDK.enNativeMissionZoneShow(getContext(), gameListener)

                        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
                            putString(FirebaseAnalyticsManager.VIEW_NAME, "에이닉 게임존")
                            putString(FirebaseAnalyticsManager.START_POINT, "inApp")
                        })
                    } catch (ex: RuntimeException) {
                        ex.printStackTrace()
                    }
                }
            }
        })
    }

    /**
     * 일별 광고 클릭 포인트 적립 요청
     * @param point
     */
    private fun saveDailyAdPoint(point: Int) {
        var m = HashMap<String, Any?>()
        m["adType"] = "GAME"
        m["clickPoint"] = point

        addDisposable(
            apiUserModel.dailyAdPoint(m)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                }, {
                    throwableCheck(it)
                })
        )
    }

    private val gameListener =
        Listener.OnGameListener { resultCode, resultMsg, point, trackingId, gameType, location ->
            try {
                if (StringUtils.isNotEmpty(resultCode) && SUCCESS_GAME == resultCode) {
                    if (location.isNotEmpty() && "banner" == location && point > 0) {
                        saveDailyAdPoint(point)
                    } else {
                        connectAnicPoint(point, trackingId.toInt(), gameType, location)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
}
