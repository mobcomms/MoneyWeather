package com.moneyweather.view

import android.content.Intent
import androidx.activity.viewModels
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.databinding.ActivityMarketingPushAgreeBinding
import com.moneyweather.event.pushsetting.PushSettingUiEvent
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.util.CustomToast
import com.moneyweather.util.PrefRepository
import com.moneyweather.viewmodel.PushSettingViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MarketingPushAgreeActivity : BaseKotlinActivity<ActivityMarketingPushAgreeBinding, PushSettingViewModel>() {

    override val layoutResourceId: Int get() = R.layout.activity_marketing_push_agree
    override val viewModel: PushSettingViewModel by viewModels()

    override fun initStartView() {
        initActionBar(
            viewDataBinding.actionBar,
            R.string.marketing_push_agree,
            ActionBarLeftButtonEnum.BACK_BUTTON
        )

        viewDataBinding.apply {
            cbPushAgree.isChecked = PrefRepository.UserInfo.marketingPushAgreed

            applyButton.setOnClickListener {
                viewModel.dispatchEvent(
                    PushSettingUiEvent.UpdatePushAgree(
                        servicePushAgreed = PrefRepository.UserInfo.servicePushAgreed,
                        marketingPushAgreed = cbPushAgree.isChecked,
                        nightPushAllowed = PrefRepository.UserInfo.nightPushAllowed
                    )
                )

                PrefRepository.UserInfo.marketingPushAgreed = cbPushAgree.isChecked

                val msg = if (cbPushAgree.isChecked) {
                    SimpleDateFormat(
                        getString(R.string.toast_ad_push_agree_confirm),
                        Locale.KOREA
                    ).format(Date())
                } else {
                    SimpleDateFormat(
                        getString(R.string.toast_ad_push_agree_reject),
                        Locale.KOREA
                    ).format(Date())
                }
                CustomToast.showToast(this@MarketingPushAgreeActivity, msg)

                setResult(RESULT_OK, Intent())
                finish()
            }
        }
    }
}