package com.moneyweather.view.fragment

import android.os.Bundle
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.viewModels
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseFragment
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.base.BaseViewPager2Adapter
import com.moneyweather.databinding.FragmentQaBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.util.FirebaseAnalyticsManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QaFragment : BaseKotlinFragment<FragmentQaBinding, BaseKotlinViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_qa
    override val viewModel: BaseKotlinViewModel by viewModels()

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "문의하기")
        })

        initActionBar(viewDataBinding.iActionBar, R.string.qa, ActionBarLeftButtonEnum.BACK_BUTTON)

        viewDataBinding.apply {
            var fragments = ArrayList<BaseFragment>()

            fragments.add(QaWriteFragment())
            fragments.add(QaListFragment())
            var tabNames = listOf<String>(getString(R.string.qa), getString(R.string.qa_confirm))
            viewPager2.adapter = BaseViewPager2Adapter(this@QaFragment, fragments, tabNames)
            viewPager2.offscreenPageLimit = tabNames.size
            //   viewPager2.isUserInputEnabled = false

        }


        childFragmentManager.setFragmentResultListener(
            "writeCallback",
            this,
            FragmentResultListener { requestKey: String, result: Bundle ->
                viewDataBinding.viewPager2.setCurrentItem(0, true)
            })

    }


    override fun onChildResume() {
        super.onChildResume()

    }

}