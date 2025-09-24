package com.moneyweather.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentSignUpBinding
import com.moneyweather.listener.OnClickDialogButtonListener
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.model.enums.DialogType
import com.moneyweather.model.enums.SignUpType
import com.moneyweather.model.enums.TermsType
import com.moneyweather.ui.dialog.HCCommonDialog
import com.moneyweather.ui.dialog.SignUpAgreeDialog
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.PrefRepository
import com.moneyweather.view.PhoneCertifiedActivity
import com.moneyweather.view.PhoneCertifiedActivity.Companion.DATA_USER_CERTIFICATION
import com.moneyweather.view.PhoneCertifiedActivity.Companion.RESULT_SUCCESS
import com.moneyweather.view.TermsActivity
import com.moneyweather.viewmodel.SignUpViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.apache.commons.lang3.StringUtils
import timber.log.Timber

@AndroidEntryPoint
class SignUpFragment : BaseKotlinFragment<FragmentSignUpBinding, SignUpViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_sign_up
    override val viewModel: SignUpViewModel by viewModels()

    private val RESULT_CODE: Int = 1122
    private var isCert: Boolean = false
    private var isVerityEmail: Boolean = false
    private var isVerityPass: Boolean = false

    @SuppressLint("SuspiciousIndentation")
    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "회원가입")
        })

        viewDataBinding.vm = viewModel

        initActionBar(
            viewDataBinding.iActionBar,
            R.string.empty,
            ActionBarLeftButtonEnum.BACK_BUTTON
        )

        viewDataBinding.apply {
            var inviteRecommendCode = PrefRepository.UserInfo.inviteRecommendCode
            if (StringUtils.isNotEmpty(inviteRecommendCode)) {
                recommendInput.setText(inviteRecommendCode)
            }

            editEmail.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    when (p0.toString().length) {
                        0 -> {
                            editEmail.visibleClearButton(false)
                            viewModel.signVerify.value = false
                        }

                        else -> {
                            editEmail.visibleClearButton(true)
                            if (!CommonUtils.isValidEmail(p0.toString())) {
                                emailError.visibility = View.VISIBLE
                                isVerityEmail = false
                            } else {
                                emailError.visibility = View.GONE
                                isVerityEmail = true
                            }
                            verityCheck()

                        }
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })

            editPassword.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    when (p0.toString().length) {
                        0 -> {
                            editPassword.visibleClearButton(false)
                            viewModel.signVerify.value = false
                        }

                        else -> {
                            editPassword.visibleClearButton(true)
                            if (CommonUtils.isValidPassword(p0.toString())) {
                                pwError.visibility = View.GONE
                                if (TextUtils.equals(p0.toString(), editPasswordConfirm.text.toString())) {
                                    pwConfirmError.visibility = View.GONE
                                    isVerityPass = true
                                } else {
                                    pwConfirmError.visibility = View.VISIBLE
                                    isVerityPass = false
                                }
                            } else {
                                pwError.visibility = View.VISIBLE
                                isVerityPass = false
                            }

                        }
                    }
                    verityCheck()
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })

            editPasswordConfirm.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    when (p0.toString().length) {
                        0 -> {
                            editPasswordConfirm.visibleClearButton(false)
                            viewModel.signVerify.value = false
                        }

                        else -> {
                            editPasswordConfirm.visibleClearButton(true)
                            if (CommonUtils.isValidPassword(p0.toString()) && TextUtils.equals(p0.toString(), editPassword.text.toString())) {
                                pwConfirmError.visibility = View.GONE
                                isVerityPass = true
                            } else {
                                pwConfirmError.visibility = View.VISIBLE
                                isVerityPass = false
                            }
                        }
                    }
                    verityCheck()
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })


            btnCert.setOnClickListener {
                val intent = Intent(context, PhoneCertifiedActivity::class.java).putExtra("signUp", true)
                startActivityForResult(intent, RESULT_CODE)
                //    viewDataBinding.btnCert.visibility = View.GONE
                //    btnNext.isEnabled = isCert && viewModel.signVerify.value == true

            }

            btnNext.setOnClickListener {
                SignUpAgreeDialog.Builder()
                    .setOnClickEventListener(object : OnClickDialogButtonListener {
                        override fun onClickCancel() {
                        }

                        override fun onClickConfirm() {
                            viewModel.postSignUp(editEmail.text.toString(), editPassword.text.toString(), recommendInput.text.toString(), "")
                        }

                        override fun onClickClose() {

                        }

                        override fun onClickTerm() {
                            val intent = Intent(context, TermsActivity::class.java)
                            intent.putExtra("type", TermsType.SERVICE)
                            startActivity(intent)
                        }

                        override fun onClickPrivacy() {
                            val intent = Intent(context, TermsActivity::class.java)
                            intent.putExtra("type", TermsType.PRIVACY)
                            startActivity(intent)
                        }
                    })
                    .setOwnerActivity(requireActivity())
                    .build(requireContext())
                    .show()

            }
        }

        viewModel.resultVerification.observe(this, Observer {
            if (it) {
                viewDataBinding.btnCert.visibility = View.GONE
                isCert = true
                viewDataBinding.btnNext.isEnabled = isCert
            } else
                isCert = false
            Toast.makeText(context, R.string.signup_fail, Toast.LENGTH_SHORT).show()
        })

        viewModel.signUpComplete.observe(this, Observer {
            if (it) {
                FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SIGN_UP, null)

                PrefRepository.UserInfo.isLogin = true
                PrefRepository.UserInfo.isFirstRun = false
                PrefRepository.UserInfo.serviceType = SignUpType.SERVICE.type
                PrefRepository.UserInfo.saveEmail = viewDataBinding.editEmail.text.toString()
                PrefRepository.UserInfo.savePassword = viewDataBinding.editPassword.text.toString()

                replaceFragment(R.id.fragmentContainer, SignUpCompleteFragment::class.java, Intent())
            } else {
                startFragment(R.id.fragmentContainer, SignUpFailFragment::class.java, Intent())
            }
        })

        viewModel.inviteFail.observe(this, Observer {
            showDialog()
        })

        viewModel.connectConfigInit()

    }

    fun verityCheck() {
        viewDataBinding.btnNext.isEnabled = isVerityEmail && isVerityPass && isCert
    }

    fun showDialog() {
        val dialog: HCCommonDialog = HCCommonDialog(requireContext())
            .setDialogType(DialogType.CONFIRM)
            .setDialogImage(R.drawable.ico_caution)
            .setDialogTitle("")
            .setContent(getString(R.string.invite_fail))
            .setPositiveButtonText(R.string.confirm)
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult is call")
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_SUCCESS) {
            data?.let {
                val isSuccess = it.getBooleanExtra(DATA_USER_CERTIFICATION, false)
                if (isSuccess) {
                    viewDataBinding.btnCert.visibility = View.GONE
                    isCert = true
                    viewDataBinding.btnNext.isEnabled = isCert
                }

            }

        }


        // viewModel.connectVerification()
    }

}