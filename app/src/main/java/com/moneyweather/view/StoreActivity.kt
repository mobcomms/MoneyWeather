package com.moneyweather.view

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.databinding.ActivitySettingBinding
import com.moneyweather.listener.AppFinishListener
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.view.fragment.StoreFragment
import com.moneyweather.viewmodel.StoreViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoreActivity : BaseKotlinActivity<ActivitySettingBinding, BaseKotlinViewModel>(), View.OnClickListener,
    AppFinishListener {

    override val layoutResourceId: Int get() = R.layout.activity_setting
    override val viewModel: StoreViewModel by viewModels()

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "상점")
            putString(FirebaseAnalyticsManager.TAB_NAME, "N페이샵")
        })

        viewDataBinding.apply {
            intent.putExtra("my", true)
            replaceFragment(R.id.fragmentContainer, StoreFragment::class.java, intent)
        }
    }

    override fun onFinish(dialog: Dialog) {

    }
}


