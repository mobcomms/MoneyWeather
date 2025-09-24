package com.moneyweather.viewmodel

import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.databinding.InverseBindingMethod
import androidx.databinding.InverseBindingMethods
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.enliple.datamanagersdk.ENDataManager
import com.enliple.datamanagersdk.events.models.ENSignUp
import com.moneyweather.R
import com.moneyweather.base.BaseApplication
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.model.enums.SignUpType
import com.moneyweather.ui.HCEditText
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.PrefRepository
import com.moneyweather.util.token.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    val apiUserModel: ApiUserModel
) : BaseKotlinViewModel() {

    private var isSaveAccount: Boolean = false

    val signVerify: MutableLiveData<Boolean> = MutableLiveData()
    val resultVerification: MutableLiveData<Boolean> = MutableLiveData()
    val signUpComplete: MutableLiveData<Boolean> = MutableLiveData()
    val inviteFail: MutableLiveData<Boolean> = MutableLiveData()

    var userEmail: Editable? = null
    var userPass: Editable? = null

    fun onClickSave(v: View) {
        when (v.isSelected) {
            true -> {
                v.isSelected = false
                isSaveAccount = false
            }

            false -> {
                v.isSelected = true
                isSaveAccount = true
            }

        }
    }

    fun connectConfigInit() {
        addDisposable(
            apiUserModel.configInit()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
//                    dismissIndicator()
                    PrefRepository.SettingInfo.verificationUrl = it.data.verificationUrl
                    PrefRepository.SettingInfo.termsUrl = it.data.termsOfServiceUrl
                    PrefRepository.SettingInfo.privacyUrl = it.data.termsOfPrivacyUrl
                    PrefRepository.SettingInfo.locationUrl = it.data.termsOfLocationUrl
                    PrefRepository.SettingInfo.companyUrl = it.data.companyUrl
                    PrefRepository.SettingInfo.inviteBannerImageUrl = it.data.inviteBannerImageUrl
                    PrefRepository.SettingInfo.externalWeatherUrl = it.data.externalWeatherUrl
                    PrefRepository.SettingInfo.externalWeatherSearchUrl = it.data.externalWeatherSearchUrl
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectConfigInit()
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

    fun connectVerification() {

        addDisposable(
            apiUserModel.verification()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {
                        resultVerification.value = true
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectVerification()
                                }
                            }

                            ResultCode.NO_VERIFIED.resultCode -> {
                                resultVerification.value = false
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

    fun postSignUp(email: String, password: String, recommend: String, ci: String) {

        var m = HashMap<String, Any?>()
        m["accessToken"] = PrefRepository.UserInfo.accessToken
        m["adid"] = PrefRepository.UserInfo.adid
        m["deviceId"] = CommonUtils.getDeviceId(BaseApplication.appContext())
        m["platform"] = SignUpType.SERVICE.type
        m["socialId"] = ""
        m["email"] = email
        m["password"] = password
        m["inviteCode"] = recommend
        m["ci"] = PrefRepository.UserInfo.ci
        m["name"] = PrefRepository.UserInfo.name
        m["phone"] = PrefRepository.UserInfo.phone
        m["birth"] = PrefRepository.UserInfo.birthday
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
                        PrefRepository.UserInfo.serviceType = SignUpType.SERVICE.type
                        PrefRepository.UserInfo.saveEmail = email
                        PrefRepository.UserInfo.savePassword = password

                        TokenStorage.saveAccessToken(data.accessToken)
                        TokenStorage.saveRefreshToken(data.refreshToken)

                        signUpComplete.value = true

                        try {
                            if (ENDataManager.isInitialized()) {
                                val signUp = ENSignUp()
                                signUp.setMemberId(data.userId.toString())
                                signUp.setEmail(email)
                                signUp.addCustomData("login_type", "email")
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
                                    postSignUp(email, password, recommend, ci)
                                }
                            }

                            ResultCode.INVITE_CODE_FAIL.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    inviteFail.value = true

                                }
                            }

                            ResultCode.DUPLICATE_ACCOUNT.resultCode,
                            ResultCode.DUPLICATE_PHONE_NUMBER.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    signUpComplete.value = false

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


    @InverseBindingMethods(
        InverseBindingMethod(
            type = HCEditText::class,
            attribute = "android:text",
            method = "getText"
        )
    )

    companion object {
        @JvmStatic
        @BindingAdapter("changeTitleColor")
        fun changeTitleColor(v: TextView?, text: String?) {
            if (v != null) {
                val first_str = v.context.getString(R.string.login_title1)
                val last_str = text
                val spannableString = SpannableString(first_str)
                val builder = SpannableStringBuilder(spannableString)

                val begin = builder.length - last_str!!.length
                val end = builder.length
                builder.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(v.context, R.color.orange_color)),
                    begin,
                    end,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
                v.text = builder
            }
        }

        @JvmStatic
        @BindingAdapter(value = ["android:textAttrChanged"])
        fun setListener(editText: HCEditText, listener: InverseBindingListener?) {
            if (listener != null) {
                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                    }

                    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                    }

                    override fun afterTextChanged(editable: Editable) {
                        listener.onChange()
                    }
                })
            }
        }

        @JvmStatic
        @BindingAdapter("android:text")
        fun setText(editText: HCEditText, text: String?) {
            text?.let {
                if (it != editText.text.toString()) {
                    editText.setText(it)
                }
            }
        }

    }


}
