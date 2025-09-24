package com.moneyweather.view.fragment

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentSunflowerSettingBinding
import com.moneyweather.event.sunflowersetting.SettingSunflowerUiEffect
import com.moneyweather.event.sunflowersetting.SettingSunflowerUiEvent
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.util.CustomToast
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.viewmodel.SunflowerSettingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SunflowerSettingFragment : BaseKotlinFragment<FragmentSunflowerSettingBinding, SunflowerSettingViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_sunflower_setting
    override val viewModel: SunflowerSettingViewModel by viewModels()

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, getString(R.string.setting_sunflower))
        })

        viewDataBinding.vm = viewModel

        initActionBar(
            viewDataBinding.iActionBar,
            R.string.setting_sunflower,
            ActionBarLeftButtonEnum.BACK_BUTTON
        )

        initViews()
        observeViewModel()

        refreshSunflowerInfo()
    }

    private fun initViews() {
        viewDataBinding.schSunflowerSettingSound.setOnClickListener {
            toggleSound()
        }

        viewDataBinding.schSunflowerSettingVibrate.setOnClickListener {
            toggleVibrate()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is SettingSunflowerUiEffect.ErrorMessage -> {
                            CustomToast.showToast(requireContext(), effect.message)
                        }
                    }
                }
            }
        }
    }

    private fun refreshSunflowerInfo() {
        lifecycleScope.launch {
            viewModel.dispatchEvent(SettingSunflowerUiEvent.FetchSettingInfo)
        }
    }

    private fun toggleSound() {
        lifecycleScope.launch {
            viewModel.dispatchEvent(SettingSunflowerUiEvent.CheckSettingSound)
        }
    }

    private fun toggleVibrate() {
        lifecycleScope.launch {
            viewModel.dispatchEvent(SettingSunflowerUiEvent.CheckSettingVibrate)
        }
    }
}