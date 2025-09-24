package com.moneyweather.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiAnicModel
import com.moneyweather.data.remote.model.ApiUserModel
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
class MissionZoneViewModel @Inject constructor(
    val apiUserModel: ApiUserModel,
    val apiAnicModel: ApiAnicModel
) : BaseKotlinViewModel() {

    var isFirst: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * 에이닉 게임 참여 1차 신청
     *
     * @param b true:락스크린 화면에서 실행, false:게임 존에서 연속 참여인 경우
     */
    fun connectAnicParticipationCheck(b: Boolean) {
        addDisposable(
            apiUserModel.anic()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    try {
                        PrefRepository.UserInfo.historyId = it.data.historyId
                        isFirst.value = b
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
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
    fun connectAnicPoint(point: Int, trackingId: Int, type: String, location: String) {
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
    fun saveDailyAdPoint(point: Int) {
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
}