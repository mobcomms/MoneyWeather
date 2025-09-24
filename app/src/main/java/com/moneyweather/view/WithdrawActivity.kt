package com.moneyweather.view

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.databinding.ActivityWithdrawBinding
import com.moneyweather.event.withdraw.WithdrawUiEvent
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.viewmodel.WithdrawViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WithdrawActivity : BaseKotlinActivity<ActivityWithdrawBinding, WithdrawViewModel>(),
    View.OnClickListener {

    override val layoutResourceId: Int get() = R.layout.activity_withdraw
    override val viewModel: WithdrawViewModel by viewModels()

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "탈퇴하기")
        })

        viewDataBinding.vm = viewModel

        initActionBar(
            viewDataBinding.iActionBar,
            R.string.withdraw,
            ActionBarLeftButtonEnum.BACK_BUTTON
        )

        viewDataBinding.apply {
            agreeLayout.setOnClickListener {
                imgCheckBox.isSelected = !imgCheckBox.isSelected
            }

            btnCancel.setOnClickListener {
                finish()
            }

            btnConfirm.setOnClickListener {
                if (imgCheckBox.isSelected) {
                    viewModel.dispatchEvent(WithdrawUiEvent.RequestWithdraw)
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }
}