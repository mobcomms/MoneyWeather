package com.moneyweather.view.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentFindPwBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.viewmodel.FindPwViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FindPwFragment : BaseKotlinFragment<FragmentFindPwBinding, FindPwViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_find_pw
    override val viewModel: FindPwViewModel by viewModels()

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "비밀번호 찾기")
        })

        viewDataBinding.vm = viewModel
        initActionBar(viewDataBinding.iActionBar, R.string.empty, ActionBarLeftButtonEnum.BACK_BUTTON)

        viewModel.sendResult.observe(this, Observer {
            if (it) {
                startFragment(R.id.fragmentContainer, CodeInputFragment::class.java)
            }
        })




        viewDataBinding.apply {

            editEmail.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    when (p0.toString().length) {
                        0 -> {
                            editEmail.visibleClearButton(false)
                        }

                        else -> {
                            editEmail.visibleClearButton(true)
                            btnNext.isEnabled = if (p0.toString().length > 1 && editPhoneNum.text.length > 9) true else false
                        }
                    }


                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })

            editPhoneNum.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    when (p0.toString().length) {
                        0 -> {
                            editPhoneNum.visibleClearButton(false)
                        }

                        else -> {
                            editPhoneNum.visibleClearButton(true)
                            btnNext.isEnabled = if (p0.toString().length > 9 && editEmail.text.length > 1) true else false
                        }
                    }


                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })


            btnNext.setOnClickListener {
                viewModel.sendEmailCode(editEmail.text.toString(), editPhoneNum.text.toString())
            }


        }

    }


}