package com.moneyweather.view.fragment

import androidx.fragment.app.viewModels
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.databinding.FragmentNewPwCompleteBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewPasswordCompleteFragment : BaseKotlinFragment<FragmentNewPwCompleteBinding, BaseKotlinViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_new_pw_complete
    override val viewModel: BaseKotlinViewModel by viewModels()

    override fun initStartView() {


        initActionBar(viewDataBinding.iActionBar, R.string.empty, ActionBarLeftButtonEnum.BACK_BUTTON)

        viewDataBinding.apply {
            btnNext.setOnClickListener {
                replaceFragment(R.id.fragmentContainer, LoginEmailFragment::class.java)
            }

        }
    }


}