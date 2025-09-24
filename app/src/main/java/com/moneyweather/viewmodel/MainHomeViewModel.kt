package com.moneyweather.viewmodel

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.enliple.banner.MobSDK
import com.enliple.banner.common.Listener
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.UrlHelper.SHOPLUS_DOMAIN
import com.moneyweather.data.remote.model.ApiAnicModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.data.remote.response.NoticeDetailResponse
import com.moneyweather.data.remote.response.ScreenPopupsActiveResponse
import com.moneyweather.model.AppInfo
import com.moneyweather.model.ShopProductItem
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.CustomToast
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.LogPrint
import com.moneyweather.util.Logger
import com.moneyweather.util.PrefRepository
import com.moneyweather.util.analytics.GaButtonClickEvent
import com.moneyweather.view.AppWebViewActivity
import com.moneyweather.view.LockScreenWebViewActivity.Companion.KEY_LOAD_URL
import com.moneyweather.view.MyPointActivity
import com.moneyweather.view.fragment.MainHomeFragment.Companion.SUCCESS_GAME
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import javax.inject.Inject

@HiltViewModel
class MainHomeViewModel @Inject constructor(
    val apiUserModel: ApiUserModel,
    val apiAnicModel: ApiAnicModel
) : BaseKotlinViewModel() {
    var resultProductList: MutableLiveData<List<ShopProductItem>> = MutableLiveData()
    var resultScreenPopupsActive: MutableLiveData<ScreenPopupsActiveResponse.Data> = MutableLiveData()
    var resultNoticeDetail: MutableLiveData<NoticeDetailResponse.Data> = MutableLiveData()

    fun connectSearchProduct(category: String,affiiliate : String, search : String, page : Int, type : Boolean) {
        addDisposable(
            apiUserModel.getProductList(category,affiiliate,search,30,page,type)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {
                        dismissIndicator()
                        if (it.result == 0) {
                            resultProductList.value = it.data.list
                        }
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectSearchProduct(category, affiiliate, search, page, type)
                                }
                            }

                            else -> {
                                Logger.e(it.errorMessage)
                            }
                        }
                    }
                })
        )
    }

    fun connectUserPoint(){
        addDisposable(
            apiUserModel.userPoint()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if(it.result == 0){
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

                            else -> {
                                Logger.e(it.errorMessage)
                            }
                        }
                    }
                })
        )
    }

    fun connectUserInfo(){
        addDisposable(
            apiUserModel.userInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
//                    dismissIndicator()
                    if(it.result == 0){
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
                            else -> {
                                Logger.e(it.errorMessage)
                            }
                        }
                    }
                })
        )
    }

    fun connectConfigMy() {
        addDisposable(
            apiUserModel.configMy()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    try {
                        it?.let { PrefRepository.SettingInfo.allowPush = it.data.allowPush }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, {
                    throwableCheck(it)
                })
        )
    }

    /**
     * 에이닉 게임 참여 1차 신청
     *
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
     *
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

    private fun connectAnicGameFail(trackingId: Int) {
        var m = HashMap<String, Any?>()
        m["tracking_id"] = trackingId
        m["fail_id"] = PrefRepository.UserInfo.userId.toInt()

        addDisposable(
            apiAnicModel.anicGameFail(m)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Logger.d("[Success] connectAnicGameFail.. resultCode=${it.result_code} resultMessage=${it.result_message}")
                }, {
                    Logger.e("[Fail] connectAnicGameFail.. e=${it.message}")
                })
        )
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

    /**
     * 공지사항 팝업 조회
     */
    fun connectScreenPopupsActive() {
        addDisposable(
            apiUserModel.screenPopupsActive()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.run { resultScreenPopupsActive.value = it.data }
                }, {
                    val body = throwableCheck(it)
                    Logger.e(body.errorMessage)
                })
        )
    }

    /**
     * 공지사항 상세 조회
     *
     * @param noticeId
     */
    fun connectNoticeDetail(noticeId: Int) {
        addDisposable(
            apiUserModel.noticeDetail(noticeId)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe{ showIndicator() }
                .doOnSuccess{ dismissIndicator() }
                .doOnError{ dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.run {
                        resultNoticeDetail.value = it.data
                    }
                }, {
                    val body = throwableCheck(it)
                    Logger.e(body.errorMessage)
                })
        )
    }

    fun onClickNaver(){
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "네이버 날씨")
            putString(FirebaseAnalyticsManager.START_POINT, "inApp")
        })

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(CommonUtils.getWeatherSearchUrl(getContext())))
        startActivity(intent)
    }

    fun onClickPoint(){
        val intent = Intent(getContext(), MyPointActivity::class.java)
        startActivity(intent)
    }

    fun onClickShopPlus() {
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

        val intent = Intent(getContext(), AppWebViewActivity::class.java)
        intent.putExtra(KEY_LOAD_URL, shopPlusUrl.toString())
        startActivity(intent)

        // Ga Log Event
        GaButtonClickEvent.logHomeShopRewardButtonClickEvent()
    }

    fun onClickGame(){
        connectAnicParticipationCheck(true)

        // Ga Log Event
        GaButtonClickEvent.logHomeGameButtonClickEvent()
    }

    private fun initAnic() {
        var userId = PrefRepository.UserInfo.userId
        LogPrint.d("game userId :: $userId")
        android.os.Handler(Looper.getMainLooper()).post(Runnable {
            MobSDK.setUserId(userId) { isSetUserSuccess: Boolean ->
                if (isSetUserSuccess) {
                    LogPrint.d("load isSetUserSuccess :: $isSetUserSuccess")
                    try {
                        MobSDK.enNativeMissionZoneShow(getContext(), gameListener)

                        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
                            putString(FirebaseAnalyticsManager.VIEW_NAME, "에이닉 게임존")
                            putString(FirebaseAnalyticsManager.START_POINT, "inApp")
                        })
                    } catch (ex: RuntimeException) {
                        val innerException = ex.cause as Exception?
                        if (innerException != null && innerException.message === "User Logout") {

                        }
                    }
                } else {
                    LogPrint.e("SetUser false")
                }
            }
        })
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
