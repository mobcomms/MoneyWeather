package com.moneyweather.viewmodel

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.moneyweather.R
import com.moneyweather.base.BaseApplication
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.data.remote.response.AppVersionResponse
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.util.CustomToast
import com.moneyweather.util.PrefRepository
import com.moneyweather.util.analytics.GaBannerClickEvent
import com.moneyweather.view.InviteFriendActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    val apiUserModel: ApiUserModel
) : BaseKotlinViewModel() {

    var resultAppVersion: MutableLiveData<AppVersionResponse.Data> = MutableLiveData()

    fun connectAppVersion() {

        addDisposable(
            apiUserModel.appVersion()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {
                        resultAppVersion.value = it.data
                    }
                }, {
                    val body = throwableCheck(it)
                    when (body.errorCode) {
                        ResultCode.SESSION_EXPIRED.resultCode -> {
                            ProcessLifecycleOwner.get().lifecycleScope.launch {
                                delay(200)
                                connectAppVersion()
                            }
                        }

                        else -> {
                            Toast.makeText(BaseApplication.appContext(), body.errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        )
    }

    fun connectConfigMy() {
        var m = HashMap<String, Any?>()
        m["themeId"] = PrefRepository.SettingInfo.selectThemeType
        m["useLockScreen"] = PrefRepository.SettingInfo.useLockScreen
        m["allowPush"] = PrefRepository.SettingInfo.allowPush

        addDisposable(
            apiUserModel.configMy(m)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {

                    }
                }, {
                    val body = throwableCheck(it)
                    when (body.errorCode) {
                        ResultCode.SESSION_EXPIRED.resultCode -> {
                            ProcessLifecycleOwner.get().lifecycleScope.launch {
                                delay(200)
                                connectConfigMy()
                            }
                        }

                        else -> {
                            Toast.makeText(BaseApplication.appContext(), body.errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        )
    }

    fun onClickInvite() {
        if (!PrefRepository.UserInfo.isLogin) {
            CustomToast.showToast(getContext(), R.string.message_non_login_user)
        } else {
            startActivity(InviteFriendActivity::class.java)

            // Ga Log Event
            GaBannerClickEvent.logInviteBannerSettingClickEvent()
        }
    }


}
