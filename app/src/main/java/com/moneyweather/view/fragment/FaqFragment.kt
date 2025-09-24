package com.moneyweather.view.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentFaqBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.model.enums.FAQCategory
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.viewmodel.FaqViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FaqFragment : BaseKotlinFragment<FragmentFaqBinding, FaqViewModel>(), OnRefreshListener {
    override val layoutResourceId: Int get() = R.layout.fragment_faq
    override val viewModel: FaqViewModel by viewModels()

    private var instanceBundle: Bundle? = null

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "FAQ")
        })

        viewDataBinding.vm = viewModel
        initActionBar(
            viewDataBinding.iActionBar,
            R.string.faq,
            ActionBarLeftButtonEnum.BACK_BUTTON
        )

        viewModel.clickListNum.observe(this, Observer {
            startFragment(
                R.id.fragmentContainer, FaqDetailFragment::class.java,
                Intent().putExtra("title", it.title).putExtra("content", it.content)
            )
        })

        viewModel.commonResultLiveData.observe(this, Observer {
            viewDataBinding.layRefresh.isRefreshing = false
        })


        viewDataBinding.apply {

            viewModel.categoryName.value = getString(R.string.faq_menu_all)
            layRefresh.setOnRefreshListener(this@FaqFragment)

            layTab.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    viewModel.categoryName.value = tab.text.toString()

                    // 적립:1, 쿠폰:3, 기타:4
                    var tabPosition = tab.position
                    if (tabPosition > 1) {
                        tabPosition += 1
                    }

                    val category: String = FAQCategory.parserToString(tabPosition)
                    viewModel.connectFaqList(category)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                }
            })

        }

        viewModel.connectFaqList(getFAQCategory())

    }

    private fun getFAQCategory(): String {
        val index: Int = viewDataBinding.layTab.getSelectedTabPosition()
        return FAQCategory.parserToString(index)
    }

    override fun onRefresh() {
        viewModel.connectFaqList(getFAQCategory())
    }


    override fun onChildResume() {
        super.onChildResume()

    }

}