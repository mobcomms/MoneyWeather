package com.moneyweather.view.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentNoticeBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.viewmodel.NoticeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeFragment : BaseKotlinFragment<FragmentNoticeBinding, NoticeViewModel>(), OnRefreshListener {
    override val layoutResourceId: Int get() = R.layout.fragment_notice
    override val viewModel: NoticeViewModel by viewModels()

    private var instanceBundle: Bundle? = null

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "공지사항")
        })

        viewDataBinding.vm = viewModel
        initActionBar(
            viewDataBinding.iActionBar,
            R.string.notice,
            ActionBarLeftButtonEnum.BACK_BUTTON
        )

        viewModel.clickListNum.observe(this, Observer {
            startFragment(
                R.id.fragmentContainer, NoticeDetailFragment::class.java,
                Intent().putExtra("title", it.title).putExtra("content", it.content).putExtra("createdAt", it.createdAt)
            )

        })

        viewModel.commonResultLiveData.observe(this, Observer {
            viewDataBinding.layRefresh.isRefreshing = false
        })


        viewDataBinding.apply {
            layRefresh.setOnRefreshListener(this@NoticeFragment)

        }


        viewModel.connectNoticeList()
    }

    override fun onRefresh() {
        viewModel.connectNoticeList()
    }


    override fun onChildResume() {
        super.onChildResume()

    }

}