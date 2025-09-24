package com.moneyweather.viewmodel

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.moneyweather.base.BaseApplication
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.data.remote.response.BaseResponse
import com.moneyweather.model.CouponItem
import com.moneyweather.model.enums.ResultCode
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CouponDetailViewModel @Inject constructor(
    val api: ApiUserModel
) : BaseKotlinViewModel() {

    var resultCoupon: MutableLiveData<CouponItem> = MutableLiveData()
    var resultCouponRefund: MutableLiveData<BaseResponse> = MutableLiveData()

    fun connectGetCouponDetail(pk: Int) {

        addDisposable(
            api.getCouponDetail(pk)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {
                        dismissIndicator()
                        resultCoupon.value = it.data
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectGetCouponDetail(pk)
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

    fun connectCouponRefund(m: HashMap<String, Any?>) {
        addDisposable(
            api.couponRefund(m)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {
                        dismissIndicator()
                        resultCouponRefund.value = it
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectCouponRefund(m)
                                }
                            }

                            else -> Toast.makeText(BaseApplication.appContext(), it.errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        )
    }


}
