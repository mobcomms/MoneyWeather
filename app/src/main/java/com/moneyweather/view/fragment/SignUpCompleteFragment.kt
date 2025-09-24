package com.moneyweather.view.fragment

import android.content.Intent
import androidx.fragment.app.viewModels
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.databinding.FragmentSignupCompleteBinding
import com.moneyweather.view.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpCompleteFragment : BaseKotlinFragment<FragmentSignupCompleteBinding, BaseKotlinViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_signup_complete
    override val viewModel: BaseKotlinViewModel by viewModels()

    override fun initStartView() {

        viewDataBinding.apply {

            val unicodeText = "${String(Character.toChars(0x1F60A))}"
            welcome.text = getString(R.string.signup_complete).plus(unicodeText)

            btnStart.setOnClickListener {
                activity?.finishAffinity()
                startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            }
        }
    }


}