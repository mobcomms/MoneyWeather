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
import com.enliple.datamanagersdk.events.models.ENSignIn
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
class LoginEmailViewModel @Inject constructor(
    val apiUserModel: ApiUserModel
) : BaseKotlinViewModel() {

    val signVerify: MutableLiveData<Boolean> = MutableLiveData()
    var loginState: MutableLiveData<Boolean> = MutableLiveData()

    fun onClickSave(v: View) {
        when (v.isSelected) {
            true -> {
                v.isSelected = false
                PrefRepository.UserInfo.saveId = false
            }

            false -> {
                v.isSelected = true
                PrefRepository.UserInfo.saveId = true
            }

        }
    }

    fun login(email: String, password: String) {
        var m = HashMap<String, Any?>()
        m["adId"] = PrefRepository.UserInfo.adid
        m["deviceId"] = CommonUtils.getDeviceId(getContext())
        m["email"] = email
        m["password"] = password
        m["platform"] = SignUpType.SERVICE.type
        m["socialId"] = ""
        m["fcm"] = PrefRepository.UserInfo.fcmToken

        addDisposable(
            apiUserModel.login(m)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.run {
                        PrefRepository.UserInfo.saveEmail = email
                        PrefRepository.UserInfo.savePassword = password
                        PrefRepository.UserInfo.isFirstRun = false
                        PrefRepository.UserInfo.serviceType = SignUpType.SERVICE.type

                        TokenStorage.saveAccessToken(data.accessToken)
                        TokenStorage.saveRefreshToken(data.refreshToken)

                        loginState.value = true

                        try {
                            if (ENDataManager.isInitialized()) {
                                val signIn = ENSignIn()
                                signIn.setMemberId(data.userId.toString())
                                signIn.addCustomData("login_type", "email")
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
                                    login(email, password)
                                }
                            }

                            else -> {
                                Toast.makeText(
                                    BaseApplication.appContext(),
                                    it.errorMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                                loginState.value = false
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
                    override fun beforeTextChanged(
                        charSequence: CharSequence,
                        i: Int,
                        i1: Int,
                        i2: Int
                    ) {

                    }

                    override fun onTextChanged(
                        charSequence: CharSequence,
                        i: Int,
                        i1: Int,
                        i2: Int
                    ) {

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
