package com.moneyweather.view

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.databinding.ActivityMyPointBinding
import com.moneyweather.event.mypoint.MyPointUiEvent
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.model.enums.DialogType
import com.moneyweather.ui.dialog.HCCommonDialog
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.view.LockScreenActivity.Companion.EXTRA_IS_LOCK_SCREEN
import com.moneyweather.viewmodel.MyPointViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyPointActivity : BaseKotlinActivity<ActivityMyPointBinding, MyPointViewModel>(),
    View.OnClickListener, OnRefreshListener {

    override val layoutResourceId: Int get() = R.layout.activity_my_point
    override val viewModel: MyPointViewModel by viewModels()

    private var fromActivity: String? = ""

    override fun initStartView() {

        intent?.let {
            fromActivity = it.getStringExtra("from_activity") ?: ""
            isLockScreen = it.getBooleanExtra(EXTRA_IS_LOCK_SCREEN, true)
        }

        val startPoint = if (isLockScreen) {
            "lockScreen"
        } else {
            "inApp"
        }

        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "나의 포인트")
            putString(FirebaseAnalyticsManager.START_POINT, startPoint)
        })

        viewDataBinding.vm = viewModel
        initActionBar(
            viewDataBinding.iActionBar,
            R.string.my_point,
            ActionBarLeftButtonEnum.BACK_BUTTON
        )

        viewModel.commonResultLiveData.observe(this, Observer {
            viewDataBinding.layRefresh.isRefreshing = false
        })


        viewModel.resultList.observe(this, Observer {
            it?.let {
                if (it.data.list.isEmpty()) {
                    viewDataBinding.recyclerPoint.setVisibility(View.GONE)
                    viewDataBinding.layEmpty.setVisibility(View.VISIBLE)
                } else {
                    viewDataBinding.recyclerPoint.setVisibility(View.VISIBLE)
                    viewDataBinding.layEmpty.setVisibility(View.GONE)
                }

                if (viewDataBinding.layRefresh.isRefreshing()) {
                    viewDataBinding.layRefresh.setRefreshing(false)
                }
            }
        })


        viewDataBinding.apply {
            layRefresh.setOnRefreshListener(this@MyPointActivity)

            tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    viewModel.connectList(tab.position, 1)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                }
            })

            layoutEnablePoint.setOnClickListener { popupAvailablePoint() }
        }

        viewModel.connectList(0, 1)
    }

    /**
     * 포인트 사용 정책 안내
     */
    private fun popupAvailablePoint() {
        val dialog = HCCommonDialog(this)
            .setDialogType(DialogType.CONFIRM)
            .setLayout(R.layout.popup_policy_point_info)
            .setPositiveButtonText(R.string.confirm)
        dialog.show()
    }

    override fun onBackPressed() {
        when (fromActivity) {
            "LockScreenActivity" -> finishAffinity()
            else -> finish()
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.dispatchEvent(MyPointUiEvent.FetchAvailablePoints)
    }

    override fun onRefresh() {
        viewModel.dispatchEvent(
            MyPointUiEvent.RefreshData(
                type = viewDataBinding.tabLayout.selectedTabPosition,
                page = 1
            )
        )
    }
}