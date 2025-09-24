package com.moneyweather.viewmodel

import android.content.Intent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.data.remote.response.AppVersionResponse
import com.moneyweather.event.EventDelegate
import com.moneyweather.event.intro.IntroUiEffect
import com.moneyweather.event.intro.IntroUiEvent
import com.moneyweather.model.enums.ActivityEnum
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.PrefRepository
import com.moneyweather.util.remoteConfig.RemoteConfigManager
import com.moneyweather.util.token.TokenStorage
import com.moneyweather.view.LoginActivity
import com.moneyweather.view.MainActivity
import com.moneyweather.view.ThemeSettingActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val remoteConfigManager: RemoteConfigManager,
    val apiUserModel: ApiUserModel
) : BaseKotlinViewModel(),
    EventDelegate<IntroUiEffect, IntroUiEvent> by EventDelegate.EventDelegateImpl() {

    override fun onCreate() {
        super.onCreate()
        observeRemoteConfig()
    }

    private fun observeRemoteConfig() {
        viewModelScope.launch {
            remoteConfigManager.configFlow.collect()
        }
    }

    override fun dispatchEvent(event: IntroUiEvent) {
        when (event) {
            is IntroUiEvent.FetchAppVersion -> {
                requestAppVersion()
            }

            is IntroUiEvent.RequestGuestLogin -> {
                postSignUpNonMember()
            }

            is IntroUiEvent.RequestLogin -> {
                postLogin(
                    moveActivity = event.moveActivity,
                    path = event.path
                )
            }

            is IntroUiEvent.RefreshSession -> {
                refreshSession(
                    moveActivity = event.moveActivity,
                    path = event.path
                )
            }

            is IntroUiEvent.MoveToLogin -> {
                startLoginActivity()
            }
        }
    }

    private fun postLogin(
        moveActivity: ActivityEnum?,
        path: String?
    ) {
        val m = HashMap<String, Any?>()
        m["adId"] = PrefRepository.UserInfo.adid
        m["deviceId"] = CommonUtils.getDeviceId(getContext())
        m["email"] = PrefRepository.UserInfo.saveEmail
        m["password"] = PrefRepository.UserInfo.savePassword
        m["platform"] = PrefRepository.UserInfo.serviceType
        m["socialId"] = PrefRepository.UserInfo.snsId
        m["fcm"] = PrefRepository.UserInfo.fcmToken

        addDisposable(
            apiUserModel.login(m)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.run {
                        TokenStorage.saveAccessToken(data.accessToken)
                        TokenStorage.saveRefreshToken(data.refreshToken)

                        startMainActivity(moveActivity, path)
                    }
                }, { throwable ->
                    throwableCheck(throwable)?.let { errorBody ->
                        when (errorBody.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    postLogin(moveActivity, path)
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
        m["themeId"] = 0

        addDisposable(
            apiUserModel.signUpGuest(m)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.run {
                        TokenStorage.saveAccessToken(data.accessToken)
                        TokenStorage.saveRefreshToken(data.refreshToken)

                        checkFirstRun()
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

    private fun refreshSession(
        moveActivity: ActivityEnum?,
        path: String?
    ) {
        addDisposable(
            apiUserModel.refreshSession()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.run {
                        TokenStorage.saveAccessToken(data.accessToken)
                        TokenStorage.saveRefreshToken(data.refreshToken)

                        startMainActivity(moveActivity, path)
                    }
                }, { throwable ->
                    throwableCheck(throwable)?.let { errorBody ->
                        when (errorBody.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode,
                            ResultCode.NO_SEARCH_USER.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    refreshSession(moveActivity, path)
                                }
                            }
                        }
                    }
                })
        )
    }

    private fun requestAppVersion() {
        addDisposable(
            apiUserModel.appVersion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    checkAppUpdate(response.data)
                }, { throwable ->
                    throwableCheck(throwable)
                    checkAppUpdate(null)
                })
        )
    }

    private fun checkAppUpdate(data: AppVersionResponse.Data?) {
        viewModelScope.launch {
            emitEffect(IntroUiEffect.CheckAppUpdate(data))
        }
    }

    private fun checkFirstRun() {
        if (PrefRepository.UserInfo.isFirstRun) {
            startThemeSettingActivity()
        } else {
            viewModelScope.launch {
                emitEffect(IntroUiEffect.CheckLogin)
            }
        }
    }

    private fun startThemeSettingActivity() {
        val intent = Intent(getContext(), ThemeSettingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        activityFinish.value = true
    }

    private fun startLoginActivity() {
        viewModelScope.launch {
            delay(SPLASH_DELAY)

            val intent = Intent(getContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            activityFinish.value = true
        }
    }

    private fun startMainActivity(
        moveActivity: ActivityEnum?,
        path: String?
    ) {
        viewModelScope.launch {
            delay(SPLASH_DELAY)

            val intent = Intent(getContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                moveActivity?.let { putExtra("moveActivity", path) }
                path?.let { putExtra("path", path) }
            }
            startActivity(intent)
            activityFinish.value = true
        }
    }

    companion object {
        private const val SPLASH_DELAY = 1_500L
    }
}