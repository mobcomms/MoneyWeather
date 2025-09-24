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
class FindIdViewModel @Inject constructor(
    val apiUserModel: ApiUserModel
) : BaseKotlinViewModel() {

    var emailResult: MutableLiveData<String> = MutableLiveData()

    fun findEmail(name: String, pno: String) {
        var m = HashMap<String, Any?>()
        m["name"] = name
        m["phone"] = pno


        addDisposable(
            apiUserModel.findEmail(m)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
//                    dismissIndicator()
                    it.run {
                        emailResult.value = it.data.maskedEmail
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    findEmail(name, pno)
                                }
                            }

                            ResultCode.NO_SEARCH_USER.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    Toast.makeText(
                                        BaseApplication.appContext(),
                                        it.errorMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            else -> {
                                Toast.makeText(
                                    BaseApplication.appContext(),
                                    it.errorMessage,
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        }
                    }
                })
        )
    }


}
