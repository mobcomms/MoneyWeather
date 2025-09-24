package com.moneyweather.viewmodel

import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.event.EventDelegate
import com.moneyweather.event.pushsetting.PushSettingUiEvent
import com.moneyweather.extensions.toBooleanYN
import com.moneyweather.extensions.toYN
import com.moneyweather.util.PrefRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class PushSettingViewModel @Inject constructor(
    val apiUserModel: ApiUserModel
) : BaseKotlinViewModel(),
    EventDelegate<Unit, PushSettingUiEvent> by EventDelegate.EventDelegateImpl() {

    override fun dispatchEvent(event: PushSettingUiEvent) {
        when (event) {
            is PushSettingUiEvent.FetchPushAgree -> {
                getPushAgree()
            }

            is PushSettingUiEvent.UpdatePushAgree -> {
                updatePushAgree(
                    servicePushAgreed = event.servicePushAgreed,
                    marketingPushAgreed = event.marketingPushAgreed,
                    nightPushAllowed = event.nightPushAllowed
                )
            }
        }
    }

    /**
     * 알림 설정 조회
     */
    private fun getPushAgree() {
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
}