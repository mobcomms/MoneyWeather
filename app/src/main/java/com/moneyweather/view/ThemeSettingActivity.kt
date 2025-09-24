package com.moneyweather.view

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.databinding.ActivitySettingBinding
import com.moneyweather.listener.AppFinishListener
import com.moneyweather.view.fragment.ThemeSettingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThemeSettingActivity : BaseKotlinActivity<ActivitySettingBinding, BaseKotlinViewModel>(), View.OnClickListener,
    AppFinishListener {

    override val layoutResourceId: Int get() = R.layout.activity_setting
    override val viewModel: BaseKotlinViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initStartView() {
        viewDataBinding.apply {
            replaceFragment(R.id.fragmentContainer, ThemeSettingFragment::class.java, intent)
        }
    }

    override fun onFinish(dialog: Dialog) {

    }

}


