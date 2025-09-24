package com.moneyweather.viewmodel

import android.content.Intent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.event.EventDelegate
import com.moneyweather.event.my.MyUiEffect
import com.moneyweather.event.my.MyUiEvent
import com.moneyweather.model.AppInfo
import com.moneyweather.model.User
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.model.enums.TermsType
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.CustomToast
import com.moneyweather.util.PrefRepository
import com.moneyweather.util.analytics.GaBannerClickEvent
import com.moneyweather.util.token.TokenStorage
import com.moneyweather.view.InviteFriendActivity
import com.moneyweather.view.LoginActivity
import com.moneyweather.view.MainActivity
import com.moneyweather.view.MyCouponActivity
import com.moneyweather.view.MyPointActivity
import com.moneyweather.view.TermsActivity
import com.moneyweather.view.WithdrawActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    val apiUserModel: ApiUserModel
) : BaseKotlinViewModel(),
    EventDelegate<MyUiEffect, MyUiEvent> by EventDelegate.EventDelegateImpl() {

    override fun dispatchEvent(event: MyUiEvent) {
        when (event) {
            MyUiEvent.Logout -> {
                logOut()
            }
        }
    }

    fun onClickLogIn() {
        startActivity(LoginActivity::class.java)
    }

    fun onClickMyPoint() {
        startActivity(MyPointActivity::class.java)
    }

    fun onClickInvite() {
        if (!PrefRepository.UserInfo.isLogin) {
            CustomToast.showToast(getContext(), R.string.message_non_login_user)
        } else {
            startActivity(InviteFriendActivity::class.java)

            // Ga Log Event
            GaBannerClickEvent.logInviteBannerMyPageClickEvent()
        }
    }

    fun onClickLogOut() {
        if (PrefRepository.UserInfo.isLogin) {
            viewModelScope.launch {
                emitEffect(MyUiEffect.ShowLogoutPopup)
            }
        } else {
            CustomToast.showToast(getContext(), R.string.message_non_login_user)
        }
    }

    fun onClickCoupon() {
        try {
            if (PrefRepository.UserInfo.isLogin) {
                startActivity(MyCouponActivity::class.java)
            } else {
                CustomToast.showToast(getContext(), R.string.message_non_login_user)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun logOut() {
        addDisposable(
            apiUserModel.logOut()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    initLoginData()
                    stopLockScreenService()
                    startLoginActivity()
                    myPageMenuSetting(isLogin = false)
                }, { throwable ->
                    throwableCheck(throwable)?.let { errorBody ->
                        when (errorBody.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    logOut()
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

    private fun postSignUpNonMember() {
        val m = HashMap<String, Any?>()
        m["adId"] = PrefRepository.UserInfo.adid
        m["deviceId"] = CommonUtils.getDeviceId(getContext())
        m["themeId"] = PrefRepository.SettingInfo.selectThemeType

        addDisposable(
            apiUserModel.signUpGuest(m)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.run {
                        TokenStorage.saveAccessToken(data.accessToken)
                        TokenStorage.saveRefreshToken(data.refreshToken)
                    }
                }, { throwable ->
                    throwableCheck(throwable)?.let { errorBody ->
                        when (errorBody.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    postSignUpNonMember()
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

        postSignUpNonMember()
    }

    private fun stopLockScreenService() {
        MainActivity.instance?.let { mainActivity ->
            mainActivity.requestNeededPermission {
                mainActivity.stopLockScreen()
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

    private fun myPageMenuSetting(isLogin: Boolean) {
        viewModelScope.launch {
            emitEffect(MyUiEffect.MyPageMenuSetting(isLogin))
        }
    }

    fun onClickWithdraw() {
        if (!PrefRepository.UserInfo.isLogin)
            CustomToast.showToast(getContext(), R.string.message_non_login_user)
        else
            startActivity(WithdrawActivity::class.java)
    }

    fun onClickTerms() {
        val intent = Intent(getContext(), TermsActivity::class.java)
        intent.putExtra("type", TermsType.SERVICE)
        startActivity(intent)
    }

    fun onClickPolicy() {
        val intent = Intent(getContext(), TermsActivity::class.java)
        intent.putExtra("type", TermsType.PRIVACY)
        startActivity(intent)
    }

    fun onClickLocation() {
        val intent = Intent(getContext(), TermsActivity::class.java)
        intent.putExtra("type", TermsType.LOCATION)
        startActivity(intent)
    }


}
