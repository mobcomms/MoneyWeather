package com.moneyweather.view.fragment

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.databinding.FragmentSignupFailBinding
import com.moneyweather.util.FirebaseAnalyticsManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFailFragment : BaseKotlinFragment<FragmentSignupFailBinding, BaseKotlinViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_signup_fail
    override val viewModel: BaseKotlinViewModel by viewModels()

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "회원가입 실패")
        })

        viewDataBinding.apply {

            btnOk.setOnClickListener {
                activity?.onBackPressed()
            }
        }
    }


}