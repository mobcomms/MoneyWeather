package com.moneyweather.viewmodel

import android.content.Intent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.enliple.datamanagersdk.ENDataManager
import com.enliple.datamanagersdk.events.models.ENSignOut
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.event.EventDelegate
import com.moneyweather.event.withdraw.WithdrawUiEvent
import com.moneyweather.model.AppInfo
import com.moneyweather.model.User
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.util.CustomToast
import com.moneyweather.util.PrefRepository
import com.moneyweather.view.LoginActivity
import com.moneyweather.view.MainActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WithdrawViewModel @Inject constructor(
    val apiUserModel: ApiUserModel
) : BaseKotlinViewModel(),
    EventDelegate<Unit, WithdrawUiEvent> by EventDelegate.EventDelegateImpl() {

    override fun dispatchEvent(event: WithdrawUiEvent) {
        when (event) {
            is WithdrawUiEvent.RequestWithdraw -> {
                connectWithDraw()
            }
        }
    }

    private fun connectWithDraw() {
        addDisposable(
            apiUserModel.withdraw()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    initLoginData()
                    stopLockScreenService()
                    startLoginActivity()
                    addTuneEvent()
                }, { throwable ->
                    throwableCheck(throwable)?.let { errorBody ->
                        when (errorBody.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectWithDraw()
                                }
                            }

                            else -> {
                                errorBody.errorMessage?.let { msg ->
                                    CustomToast.showToast(getContext(), msg)
                                }
                            }
                        }
                    }
                })
        )
    }

    private fun initLoginData() {
        AppInfo.setUserInfo(
            User(
                email = getString(R.string.guest),
                name = getString(R.string.user_type_guest),
                phone = "",
                userId = ""
            )
        )

        PrefRepository.UserInfo?.let { userInfo ->
            userInfo.clear()
            userInfo.isLogin = false
        }
    }

    private fun stopLockScreenService() {
        PrefRepository.SettingInfo.useLockScreen = false

        MainActivity.instance?.let { mainActivity ->
            mainActivity.requestNeededPermission {
                mainActivity.startLockScreen()
            }
        }
    }

    private fun startLoginActivity() {
        val intent = Intent(getContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        activityFinish.value = true
    }

    private fun addTuneEvent() {
        if (ENDataManager.isInitialized()) {
            val signOut = ENSignOut()
            signOut.setMemberId(PrefRepository.UserInfo.userId)
            ENDataManager.getInstance().addEvent(signOut)
        }
    }
}
