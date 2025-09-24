package com.moneyweather.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.moneyweather.BuildConfig
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiMobwithModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.event.EventDelegate
import com.moneyweather.event.charge.ChargeUiEffect
import com.moneyweather.event.charge.ChargeUiEvent
import com.moneyweather.extensions.retryWithBackoff
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.util.PrefRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChargeViewModel @Inject constructor(
    val apiUserModel: ApiUserModel,
    val apiMobwithModel: ApiMobwithModel
) : BaseKotlinViewModel(),
    EventDelegate<ChargeUiEffect, ChargeUiEvent> by EventDelegate.EventDelegateImpl() {

    var bannerScript: MutableLiveData<String> = MutableLiveData()
    var isAvailableOfferWallAd: MutableLiveData<Boolean> = MutableLiveData()

    private val _buzzvilAvailable: MutableStateFlow<Boolean> = MutableStateFlow(PrefRepository.LockQuickInfo.chargeBuzzvilAvailable)
    val buzzvilAvailable: StateFlow<Boolean> = _buzzvilAvailable.asStateFlow()

    override fun dispatchEvent(event: ChargeUiEvent) {
        when (event) {
            is ChargeUiEvent.CheckVerification -> {
                checkVerification()
            }
        }
    }

    /**
     * 모비위드 스크립트 배너
     */
    fun getMobwithScriptBanner() {
        var m = HashMap<String, Any?>()
        m["zone"] = BuildConfig.MOBWITH_ZONE_ID_320_100
        m["count"] = 1 // 노출 광고 개수(현재 최대치는 1임)
        m["w"] = 320
        m["h"] = 100
        m["adid"] = PrefRepository.UserInfo.adid

        addDisposable(
            apiMobwithModel.getMobwithScriptBanner(m)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ script ->
                    try {
                        bannerScript.value = script.string()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, {
                    throwableCheck(it)
                })
        )
    }

    private fun checkVerification() {
        addDisposable(
            apiUserModel.verification()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .retryWithBackoff()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showBuzzvilOfferWall()
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.NO_VERIFIED.resultCode -> {
                                showVerification()
                            }

                            else -> {
                                showToast(it.errorMessage ?: "Unknown Error")
                            }
                        }
                    }
                })
        )
    }

    private fun showVerification() {
        viewModelScope.launch {
            emitEffect(ChargeUiEffect.ShowVerification)
        }
    }

    private fun showBuzzvilOfferWall() {
        viewModelScope.launch {
            emitEffect(ChargeUiEffect.ShowBuzzvilOfferWall)
        }
    }

    private fun showToast(message: String) {
        viewModelScope.launch {
            emitEffect(ChargeUiEffect.ShowToast(message))
        }
    }

    /**
     * 일별 광고 클릭 포인트 현황 조회
     */
    fun checkDailyAdPoint() {
        addDisposable(
            apiUserModel.dailyAdPoint()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    try {
                        isAvailableOfferWallAd.value = it.data.isAvailableOfferWallAd
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, {
                    throwableCheck(it)
                })
        )
    }

    /**
     * 일별 광고 클릭 포인트 적립 요청
     */
    fun saveDailyAdPoint() {
        var m = HashMap<String, Any?>()
        m["adType"] = "OFFER_WALL"
        m["clickPoint"] = 1

        addDisposable(
            apiUserModel.dailyAdPoint(m)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    checkDailyAdPoint()
                }, {
                    throwableCheck(it)
                })
        )
    }
}