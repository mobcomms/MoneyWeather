package com.moneyweather.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.data.remote.response.AutoMissionResponse.Mission
import com.moneyweather.event.EventDelegate
import com.moneyweather.event.theme.ThemeUiEffect
import com.moneyweather.event.theme.ThemeUiEvent
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.util.CustomToast
import com.moneyweather.util.Logger
import com.moneyweather.util.PrefRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LockScreenWebViewViewModel @Inject constructor(
    val apiUserModel: ApiUserModel
) : BaseKotlinViewModel(),
    EventDelegate<ThemeUiEffect, ThemeUiEvent> by EventDelegate.EventDelegateImpl() {

    var isPoMissionAuto: MutableLiveData<Boolean> = MutableLiveData()
    var isMobonReward: MutableLiveData<Boolean> = MutableLiveData()
    var isNewsReward: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * @param mission
     * @param step 미션 스크립트 이슈 확인용
     */
    fun poMissionAuto(mission: Mission, step: String) {
        var m = HashMap<String, Any?>()
        m["mission_seq"] = mission.mission_seq
        m["mission_id"] = mission.mission_id
        m["access_token"] = PrefRepository.UserInfo.xAccessToken
        m["ad_id"] = PrefRepository.UserInfo.adid
        m["mission_point"] = mission.user_point

        /**
         * [정책]
         *  - ""인 경우 미션 정상 동작
         *  - 0인 경우 타이머 시간이 지나 미션이 강제 종료된 케이스
         *  - 1이상인 경우 미션이 멈춘 화면 페이지
         */
        m["step"] = step ?: ""

        addDisposable(
            apiUserModel.poMissionAuto(m)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {
                        try {
                            when (result) {
                                0 -> {
                                    Logger.d("poMissionAuto Success")
                                    isPoMissionAuto.value = true
                                }

                                else -> {
                                    Logger.e("poMissionAuto Fail.. errorCode=${result}")
                                    isPoMissionAuto.value = false
                                }
                            }
                        } catch (e: Exception) {
                            Logger.e(e.message)
                            isPoMissionAuto.value = false
                        }
                    }
                }, {
                    val body = throwableCheck(it)
                    Logger.e(body.errorMessage)
                    CustomToast.showToast(getContext(), body.errorMessage.toString())
                    isPoMissionAuto.value = false
                })
        )
    }

    /**
     * 모비온 라이브 캠페인 - 포인트 적립 요청
     */
    fun mobonReward() {
        addDisposable(
            apiUserModel.mobonReward()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    isMobonReward.value = true
                }, {
                    isMobonReward.value = false

                    val body = throwableCheck(it)
                    body?.let {
                        when (body.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    mobonReward()
                                }
                            }

                            else -> {
                                Logger.e(body.errorMessage)
                            }
                        }
                    }
                })
        )
    }

    /**
     * 오아시스 뉴스 조회 리워드 지급 요청
     * @param guid
     * @param point
     */
    fun newsReward(guid: String, point: Int) {
        addDisposable(
            apiUserModel.newsReward(guid)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    isNewsReward.value = true
                    CustomToast.showToast(getContext(), "${point}P 적립 완료되었습니다")
                }, {
                    isNewsReward.value = false
                    val body = throwableCheck(it)
                    CustomToast.showToast(getContext(), body.errorMessage.toString())
                })
        )
    }
}