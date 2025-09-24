package com.moneyweather.view.fragment

import androidx.fragment.app.viewModels
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentQaDetailBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.viewmodel.QaDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QaDetailFragment : BaseKotlinFragment<FragmentQaDetailBinding, QaDetailViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_qa_detail
    override val viewModel: QaDetailViewModel by viewModels()

    private var inquiryId: Int = 0

    override fun initStartView() {
        viewDataBinding.vm = viewModel
        initActionBar(
            viewDataBinding.iActionBar,
            R.string.qa,
            ActionBarLeftButtonEnum.BACK_BUTTON
        )
        arguments?.let {
            inquiryId = it.getInt("inquiryId")
            viewModel.connectQaDetail(inquiryId)
        }

    }


    override fun onChildResume() {
        super.onChildResume()

    }

}