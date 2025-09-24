package com.moneyweather.view

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.databinding.ActivityPushSettingBinding
import com.moneyweather.event.pushsetting.PushSettingUiEvent
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.util.PrefRepository
import com.moneyweather.viewmodel.PushSettingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PushSettingActivity : BaseKotlinActivity<ActivityPushSettingBinding, PushSettingViewModel>() {

    override val layoutResourceId: Int get() = R.layout.activity_push_setting
    override val viewModel: PushSettingViewModel by viewModels()

    override fun initStartView() {
        initActionBar(
            viewDataBinding.actionBar,
            R.string.setting_push,
            ActionBarLeftButtonEnum.BACK_BUTTON
        )

        setPushSwitch()
        onClickListener()
    }

    private fun setPushSwitch() {
        viewDataBinding.apply {
            PrefRepository.UserInfo.apply {
                setMarketingPushStatusText(marketingPushAgreed)

                switchServicePush.isChecked = servicePushAgreed
                switchMarketingPush.isChecked = marketingPushAgreed
                switchNightPush.isChecked = nightPushAllowed
            }
        }
    }

    private fun setMarketingPushStatusText(isAgreed: Boolean) {
        viewDataBinding.tvMarketingPushStatus.text = if (isAgreed) {
            resources.getString(R.string.agreed)
        } else {
            resources.getString(R.string.rejected)
        }
    }

    private fun onClickListener() {
        viewDataBinding.apply {
            switchServicePush.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    viewModel.dispatchEvent(
                        PushSettingUiEvent.UpdatePushAgree(
                            servicePushAgreed = isChecked,
                            marketingPushAgreed = PrefRepository.UserInfo.marketingPushAgreed,
                            nightPushAllowed = PrefRepository.UserInfo.nightPushAllowed
                        )
                    )
                }
            }

            switchMarketingPush.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    setMarketingPushStatusText(isChecked)

                    viewModel.dispatchEvent(
                        PushSettingUiEvent.UpdatePushAgree(
                            servicePushAgreed = PrefRepository.UserInfo.servicePushAgreed,
                            marketingPushAgreed = isChecked,
                            nightPushAllowed = PrefRepository.UserInfo.nightPushAllowed
                        )
                    )
                }
            }

            switchNightPush.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    viewModel.dispatchEvent(
                        PushSettingUiEvent.UpdatePushAgree(
                            servicePushAgreed = PrefRepository.UserInfo.servicePushAgreed,
                            marketingPushAgreed = PrefRepository.UserInfo.marketingPushAgreed,
                            nightPushAllowed = isChecked
                        )
                    )
                }
            }

            clMarketingPushAgreeLayout.setOnClickListener {
                val intent = Intent(this@PushSettingActivity, MarketingPushAgreeActivity::class.java)
                activityResultLauncher.launch(intent)
            }
        }
    }

    private var activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (RESULT_OK == result.resultCode) {
            setPushSwitch()
        }
    }
}