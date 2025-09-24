package com.moneyweather.viewmodel

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.enliple.datamanagersdk.ENDataManager
import com.enliple.datamanagersdk.events.models.ENSignIn
import com.enliple.datamanagersdk.events.models.ENSignUp
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseApplication
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.model.enums.SignUpType
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.Logger
import com.moneyweather.util.PrefRepository
import com.moneyweather.util.token.TokenStorage
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    val apiUserModel: ApiUserModel
) : BaseKotlinViewModel() {

    var isViewInit: MutableLiveData<Boolean> = MutableLiveData()
    private var googleLoginModule: GoogleSignInClient? = null
    var loginResult: MutableLiveData<Boolean> = MutableLiveData()


    @SuppressLint("SuspiciousIndentation")
    fun postSignUp(loginType: SignUpType, snsId: String, email: String, name: String) {

        var m = HashMap<String, Any?>()
        m["accessToken"] = PrefRepository.UserInfo.accessToken
        m["adid"] = PrefRepository.UserInfo.adid
        m["deviceId"] = CommonUtils.getDeviceId(BaseApplication.appContext())
        m["platform"] = loginType.type
        m["socialId"] = snsId
        m["email"] = email
        m["password"] = PrefRepository.UserInfo.savePassword
        m["ci"] = ""
        m["name"] = name
        m["phone"] = ""
        m["birth"] = ""
        m["sex"] = 0
        m["appVersion"] = CommonUtils.getAppVersion()

        addDisposable(
            apiUserModel.signUpUser(m)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {
                        PrefRepository.UserInfo.isLogin = true
                        PrefRepository.UserInfo.serviceType = loginType.type
                        PrefRepository.UserInfo.snsId = snsId

                        TokenStorage.saveAccessToken(data.accessToken)
                        TokenStorage.saveRefreshToken(data.refreshToken)

                        loginResult.value = true

                        try {
                            if (ENDataManager.isInitialized()) {
                                val signUp = ENSignUp()
                                signUp.setMemberId(data.userId.toString())
                                signUp.setEmail(email)

                                var type = ""
                                when (loginType.type) {
                                    SignUpType.KAKAO.type -> type = "kakao"
                                    SignUpType.NAVER.type -> type = "naver"
                                    SignUpType.GOOGLE.type -> type = "google"
                                    else -> ""
                                }
                                if (type.isNotEmpty()) {
                                    signUp.addCustomData("login_type", type)
                                }

                                ENDataManager.getInstance().addEvent(signUp)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    postSignUp(loginType, snsId, email, name)
                                }
                            }

                            ResultCode.DUPLICATE_USER.resultCode,
                            ResultCode.NO_SEARCH_USER.resultCode -> {
                                postLogin(email, loginType.type, snsId)
                            }

                            else -> {
                                PrefRepository.UserInfo.serviceType = ""
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

    fun postLogin(email: String, platform: String, snsId: String) {

        var m = HashMap<String, Any?>()
        m["adId"] = PrefRepository.UserInfo.adid
        m["deviceId"] = CommonUtils.getDeviceId(BaseApplication.appContext())
        m["email"] = email
        m["password"] = PrefRepository.UserInfo.savePassword
        m["platform"] = platform
        m["socialId"] = snsId
        m["fcm"] = PrefRepository.UserInfo.fcmToken

        addDisposable(
            apiUserModel.login(m)
                .doAfterSuccess {}
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.run {
                        PrefRepository.UserInfo.isLogin = true
                        PrefRepository.UserInfo.serviceType = platform

                        TokenStorage.saveAccessToken(data.accessToken)
                        TokenStorage.saveRefreshToken(data.refreshToken)

                        loginResult.value = true

                        try {
                            if (ENDataManager.isInitialized()) {
                                val signIn = ENSignIn()
                                signIn.setMemberId(data.userId.toString())

                                var type = ""
                                when (platform) {
                                    SignUpType.KAKAO.type -> type = "kakao"
                                    SignUpType.NAVER.type -> type = "naver"
                                    SignUpType.GOOGLE.type -> type = "google"
                                    else -> ""
                                }
                                if (type.isNotEmpty()) {
                                    signIn.addCustomData("login_type", type)
                                }

                                ENDataManager.getInstance().addEvent(signIn)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }


                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    postLogin(email, platform, snsId)
                                }
                            }

                            else -> {
                                Toast.makeText(BaseApplication.appContext(), it.errorMessage, Toast.LENGTH_SHORT).show()
                                loginResult.value = false
                            }
                        }
                    }

                })
        )
    }

    fun connectConfigMy() {
        var m = HashMap<String, Any?>()
        m["themeId"] = PrefRepository.SettingInfo.selectThemeType
        m["useLockScreen"] = PrefRepository.SettingInfo.useLockScreen
        m["allowPush"] = false

        addDisposable(
            apiUserModel.configMy(m)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {
                        Logger.d("configMy Success..")
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

    fun viewInit() {
        isViewInit.value = true
    }

    fun naverLogin() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "네이버 로그인")
        })

        val profileCallback = object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(response: NidProfileResponse) {
                FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
                    putString(FirebaseAnalyticsManager.LOGIN_TYPE, "naver")
                })

                val userId = response.profile?.id ?: ""
                val email = response.profile?.email ?: ""
                val name = response.profile?.name ?: ""

                serviceLogin(SignUpType.NAVER, userId, email, name)
            }

            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Toast.makeText(
                    getContext(), "errorCode: ${errorCode}\n" +
                            "errorDescription: ${errorDescription}", Toast.LENGTH_SHORT
                ).show()
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }

        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                NidOAuthLogin().callProfileApi(profileCallback)
            }

            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Logger.v("errorCode :  $errorCode errorDesc : $errorDescription")
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }
        NaverIdLoginSDK.authenticate(getContext(), oauthLoginCallback)
    }

    /**
     * 연동해제
     * 네이버 아이디와 애플리케이션의 연동을 해제하는 기능은 다음과 같이 NidOAuthLogin().callDeleteTokenApi() 메서드로 구현합니다.
    연동을 해제하면 클라이언트에 저장된 토큰과 서버에 저장된 토큰이 모두 삭제됩니다.
     */
    private fun startNaverDeleteToken() {
        NidOAuthLogin().callDeleteTokenApi(getContext(), object : OAuthLoginCallback {
            override fun onSuccess() {
                //서버에서 토큰 삭제에 성공한 상태입니다.
//                Toast.makeText(this@MainActivity, "네이버 아이디 토큰삭제 성공!", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(httpStatus: Int, message: String) {
                // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                Log.d("naver`", "errorCode: ${NaverIdLoginSDK.getLastErrorCode().code}")
                Log.d("naver", "errorDesc: ${NaverIdLoginSDK.getLastErrorDescription()}")
            }

            override fun onError(errorCode: Int, message: String) {
                // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                onFailure(errorCode, message)
            }
        })
    }

    fun serviceLogin(signUpType: SignUpType, snsId: String, email: String, name: String) {
        Logger.d("serviceLogin : ${signUpType.type} , $snsId")
        PrefRepository.UserInfo.snsId = snsId
        PrefRepository.UserInfo.saveEmail = email
        PrefRepository.UserInfo.name = name
        postSignUp(signUpType, snsId, email, name)

    }


    companion object {

        @JvmStatic
        @BindingAdapter("changeColor")
        fun changeColor(v: TextView?, text: String?) {
            if (v != null) {

                val first_str = v.context.getString(R.string.login_title3_1)
                val last_str = v.context.getString(R.string.login_title3_2)
                val spannableString = SpannableString(first_str)
                val builder = SpannableStringBuilder(spannableString)
                builder.append(last_str)
                val begin = 0
                val end = first_str.length
                builder.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            v.context,
                            R.color.orange_color
                        )
                    ),
                    begin,
                    end,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
                v.text = builder
            }
        }
    }


}
