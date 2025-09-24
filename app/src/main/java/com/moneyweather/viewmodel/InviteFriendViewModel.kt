package com.moneyweather.viewmodel

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.moneyweather.R
import com.moneyweather.base.BaseApplication
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.data.remote.response.InviteResponse
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.util.CustomToast
import com.moneyweather.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InviteFriendViewModel @Inject constructor(
    val apiUserModel: ApiUserModel
) : BaseKotlinViewModel() {

    val resultData: MutableLiveData<InviteResponse.Data> = MutableLiveData()
    var isRedeemed: MutableLiveData<Boolean> = MutableLiveData()
    var isRedeemedSocial: MutableLiveData<Boolean> = MutableLiveData()

    fun connectInviteInfo() {
        addDisposable(
            apiUserModel.getInviteInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.run {
                        resultData.value = it.data
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectInviteInfo()
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

    fun inviteRedeem(inviteCode: String) {
        var m = HashMap<String, Any?>()
        m["inviteCode"] = inviteCode

        addDisposable(
            apiUserModel.inviteRedeem(m)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {
                        Logger.d("inviteRedeem Success")
                        isRedeemed.value = true
                        Toast.makeText(getContext(), getString(R.string.invite_success), Toast.LENGTH_SHORT).show()
                    }
                }, {
                    isRedeemed.value = false
                    val body = throwableCheck(it)
                    Logger.d("inviteRedeem Fail.. code=${body.errorCode} msg=${body.errorMessage}")
                    when (body.errorCode) {
                        ResultCode.SESSION_EXPIRED.resultCode -> {
                            ProcessLifecycleOwner.get().lifecycleScope.launch {
                                delay(200)
                            }
                        }

                        else -> {
                            Toast.makeText(BaseApplication.appContext(), body.errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        )
    }

    /**
     * @param inviteCode
     */
    fun inviteRedeemSocial(inviteCode: String) {
        var m = HashMap<String, Any?>()
        m["inviteCode"] = inviteCode

        addDisposable(
            apiUserModel.inviteRedeemSocial(m)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    try {
                        it.run {
                            isRedeemedSocial.value = true
                            CustomToast.showToast(getContext(), getString(R.string.invite_success))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, {
                    try {
                        isRedeemedSocial.value = false
                        val body = throwableCheck(it)
                        CustomToast.showToast(getContext(), body.errorMessage.toString())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                })
        )
    }
}
