package com.moneyweather.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.databinding.ActivityRecommendCodeInputBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.util.PrefRepository
import com.moneyweather.viewmodel.InviteFriendViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecommendCodeInputActivity : BaseKotlinActivity<ActivityRecommendCodeInputBinding, InviteFriendViewModel>() {

    override val layoutResourceId: Int get() = R.layout.activity_recommend_code_input
    override val viewModel: InviteFriendViewModel by viewModels()

    override fun initStartView() {

        initActionBar(
            viewDataBinding.iActionBar,
            R.string.recommend_input2,
            ActionBarLeftButtonEnum.BACK_BUTTON
        )

        viewDataBinding.apply {
            val inviteRecommendCode = PrefRepository.UserInfo.inviteRecommendCode
            if (inviteRecommendCode.isNotEmpty()) {
                recommendInput.setText(inviteRecommendCode)
            }

            btnNextEnabled()

            recommendInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    btnNextEnabled()
                }

                override fun afterTextChanged(s: Editable?) {

                }
            })

            btnNext.setOnClickListener {
                val recommendCode = recommendInput.text.toString()
                if (recommendCode.isNotEmpty()) {
                    viewModel.inviteRedeemSocial(recommendCode)
                    startApp()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(
            this@RecommendCodeInputActivity, onBackPressedCallback
        )
    }

    override fun onClickActionBarLeftButton() {
        startApp()
    }

    private fun btnNextEnabled() {
        viewDataBinding.apply {
            btnNext.isEnabled = recommendInput.text.isNotEmpty()
        }
    }

    private fun startApp() {
        PrefRepository.UserInfo.inviteRecommendCode = ""
        startActivity(MainActivity::class.java)
        this@RecommendCodeInputActivity.finish()
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startApp()
            }
        }
}