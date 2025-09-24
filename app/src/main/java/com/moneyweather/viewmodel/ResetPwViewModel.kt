package com.moneyweather.viewmodel

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.moneyweather.base.BaseApplication
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.model.enums.ResultCode
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPwViewModel @Inject constructor(
    val apiUserModel: ApiUserModel
) : BaseKotlinViewModel() {

    var resetResult: MutableLiveData<Boolean> = MutableLiveData()

    fun sendNewPass(code: String, password: String) {
        var m = HashMap<String, Any?>()
        m["resetCode"] = code
        m["newPassword"] = password

        addDisposable(
            apiUserModel.sendNewPass(m)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.run {
                        resetResult.value = true
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
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


}
