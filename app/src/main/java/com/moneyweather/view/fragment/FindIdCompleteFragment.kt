package com.moneyweather.view.fragment

import androidx.fragment.app.viewModels
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.databinding.FragmentFindIdCompleteBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FindIdCompleteFragment : BaseKotlinFragment<FragmentFindIdCompleteBinding, BaseKotlinViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_find_id_complete
    override val viewModel: BaseKotlinViewModel by viewModels()

    private var email: String? = ""

    override fun initStartView() {

        initActionBar(viewDataBinding.iActionBar, R.string.empty, ActionBarLeftButtonEnum.BACK_BUTTON)

        arguments?.let {
            email = it.getString("email")
        }

        viewDataBinding.apply {
            emailBox.text = email

            btnNext.setOnClickListener {
                replaceFragment(R.id.fragmentContainer, LoginEmailFragment::class.java)
            }
        }
    }


}