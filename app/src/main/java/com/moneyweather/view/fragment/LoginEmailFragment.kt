package com.moneyweather.view.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentEmailLoginBinding
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.PrefRepository
import com.moneyweather.view.MainActivity
import com.moneyweather.viewmodel.LoginEmailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginEmailFragment : BaseKotlinFragment<FragmentEmailLoginBinding, LoginEmailViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_email_login
    override val viewModel: LoginEmailViewModel by viewModels()

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "이메일 로그인")
        })

        viewDataBinding.vm = viewModel
        viewDataBinding.apply {

            if (PrefRepository.UserInfo.saveId) {
                PrefRepository.UserInfo.saveEmail.let {
                    editEmail.setText(it)
                }
                saveAccount.isSelected = true
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
                            if (editPassword.text.length > 7)
                                viewModel.signVerify.value = true
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
                            if (p0.toString().length in 8..32)
                                viewModel.signVerify.value = true
                        }
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })

            btnSignUp.setOnClickListener {
                startFragment(R.id.fragmentContainer, SignUpFragment::class.java)
            }

            btnSearchEmail.setOnClickListener {
                startFragment(R.id.fragmentContainer, FindIdFragment::class.java)
            }

            btnSearchPw.setOnClickListener {
                startFragment(R.id.fragmentContainer, FindPwFragment::class.java)
            }

            btnPrev.setOnClickListener {
                activity?.onBackPressed()
            }

            btnLogin.setOnClickListener {
                viewModel.login(editEmail.text.toString(), editPassword.text.toString())
            }


            viewModel.loginState.observe(this@LoginEmailFragment, Observer {
                when (it) {
                    true -> {
                        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
                            putString(FirebaseAnalyticsManager.LOGIN_TYPE, "email")
                        })

                        PrefRepository.UserInfo.isLogin = true
                        activity?.finishAffinity()
                        startActivity(MainActivity::class.java)
                    }

                    false -> {

                    }
                }
            })

            rootLayout.setOnClickListener { }
        }

    }


}