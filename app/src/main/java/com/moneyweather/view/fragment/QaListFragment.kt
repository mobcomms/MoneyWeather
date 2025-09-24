package com.moneyweather.view.fragment

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentQaListBinding
import com.moneyweather.viewmodel.QaListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QaListFragment : BaseKotlinFragment<FragmentQaListBinding, QaListViewModel>(), OnRefreshListener {
    override val layoutResourceId: Int get() = R.layout.fragment_qa_list
    override val viewModel: QaListViewModel by viewModels()

    private var instanceBundle: Bundle? = null

    override fun initStartView() {
        viewDataBinding.vm = viewModel


        viewModel.clickListNum.observe(this, Observer {
            startFragment(
                R.id.fragmentContainer, QaDetailFragment::class.java,
                Intent().putExtra("inquiryId", it)
            )

        })

        viewModel.commonResultLiveData.observe(this, Observer {
            viewDataBinding.layRefresh.isRefreshing = false
        })


        viewDataBinding.apply {
            layRefresh.setOnRefreshListener(this@QaListFragment)

            qaBtn.setOnClickListener {
                parentFragmentManager.setFragmentResult(
                    "writeCallback",
                    bundleOf("write" to "y")
                )
            }

        }


        viewModel.connectQaList()
    }

    override fun onRefresh() {
        viewModel.connectQaList()
    }


    override fun onChildResume() {
        super.onChildResume()

    }

}