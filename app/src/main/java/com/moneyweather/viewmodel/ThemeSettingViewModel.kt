package com.moneyweather.viewmodel

import android.widget.Toast
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.moneyweather.base.BaseApplication
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.util.PrefRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeSettingViewModel @Inject constructor(
    val api: ApiUserModel
) : BaseKotlinViewModel() {

    fun connectConfigMy() {
        var m = HashMap<String, Any?>()
        if (PrefRepository.UserInfo.isFirst) {
            m["themeId"] = 0
        } else {
            m["themeId"] = PrefRepository.SettingInfo.selectThemeType
        }
        m["useLockScreen"] = PrefRepository.SettingInfo.useLockScreen
        m["allowPush"] = false

        addDisposable(
            api.configMy(m)
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


}
