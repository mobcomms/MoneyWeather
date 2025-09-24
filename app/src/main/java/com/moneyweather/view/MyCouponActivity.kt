package com.moneyweather.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.adapter.CouponAdapter
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.databinding.ActivityMyCouponBinding
import com.moneyweather.model.CouponItem
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.model.enums.DialogType
import com.moneyweather.ui.dialog.HCCommonDialog
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.viewmodel.MyCouponViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyCouponActivity : BaseKotlinActivity<ActivityMyCouponBinding, MyCouponViewModel>(), View.OnClickListener,
    OnRefreshListener {

    override val layoutResourceId: Int get() = R.layout.activity_my_coupon
    override val viewModel: MyCouponViewModel by viewModels()

    private val REQUEST_CODE: Int = 1101
    private val REQUEST_CODE_DETAIL: Int = 1111
    private var mCouponAdapter: CouponAdapter? = null
    private var mCurrentPage: Int = 0
    private var mCouponPk: Int = 0


    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "쿠폰함")
        })

        viewDataBinding.vm = viewModel
        initActionBar(viewDataBinding.iActionBar, R.string.my_coupon_box, ActionBarLeftButtonEnum.BACK_BUTTON)
        initView()
        checkVerification()


        viewModel.resultCouponList.observe(this, Observer {
            viewDataBinding.progress.visibility = View.GONE

            if (it.data.list.size > 0) {
                dataSetChanged(mCurrentPage, it.data.list)
            } else {
                viewDataBinding.apply {
                    progress.setVisibility(View.GONE)
                    groupNotice.setVisibility(View.GONE)
                    recyclerCoupon.setVisibility(View.GONE)
                    layDataLoadFailureView.setVisibility(View.GONE)
                    layEmpty.setVisibility(View.VISIBLE)
                }
            }

            viewDataBinding.apply {
                if (layRefresh.isRefreshing()) layRefresh.setRefreshing(false)
            }

        })

        viewModel.resultVerification.observe(this, androidx.lifecycle.Observer {
            when (it) {
                true -> {
                    onRefresh()
                }

                false -> {
                    verificationPopup()
                }
            }

        })

    }

    fun initView() {
        viewDataBinding.apply {
            layRefresh.setOnRefreshListener(this@MyCouponActivity)
            mCouponAdapter = CouponAdapter(this@MyCouponActivity)
            if (recyclerCoupon.getLayoutManager() == null) {
                recyclerCoupon.setLayoutManager(LinearLayoutManager(applicationContext))
            }
            (recyclerCoupon.getItemAnimator() as SimpleItemAnimator).supportsChangeAnimations =
                false
            recyclerCoupon.setAdapter(mCouponAdapter)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onClick(view: View?) {
        view?.let {
            val id = it.id
            if (id == R.id.btnRetry) {
                getCouponList(0)
            }
        }

    }

    fun checkVerification() {
        viewModel.connectVerification()
    }

    override fun onRefresh() {
        getCouponList(0)
    }

    fun getCouponList(page: Int) {
        val param = HashMap<String, Any?>()
        param["page"] = page.toString()
        param["showCount"] = "20"

        mCurrentPage = page ?: 0

        viewDataBinding.progress.visibility = View.VISIBLE
        viewModel.connectGetCouponList(param)
    }

    fun requestCouponState() {
        viewModel.connectRequestCouponState(mCouponPk)
    }

    private fun dataSetChanged(page: Int, coupons: ArrayList<CouponItem>) {
        viewDataBinding.apply {
            groupNotice.setVisibility(View.VISIBLE)
            recyclerCoupon.setVisibility(View.VISIBLE)
            layDataLoadFailureView.setVisibility(View.GONE)
            layEmpty.setVisibility(View.GONE)

            mCouponAdapter?.setData(page, coupons)
        }

    }

    fun startDetailActivity(couponPk: Int) {
        mCouponPk = couponPk

        val intent = Intent(this, CouponDetailActivity::class.java)
        intent.putExtra("KEY_COUPON_PK", couponPk)
        startActivityForResult(intent, REQUEST_CODE_DETAIL)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_DETAIL) {
            if (mCouponPk > 0) {
                requestCouponState()
            }
        } else if (requestCode == REQUEST_CODE) {
            checkVerification()
        }
    }

    fun verificationPopup() {
        val dialog = HCCommonDialog(this)
            .setDialogType(DialogType.ALERT)
            .setDialogTitle(R.string.certified)
            .setContent(R.string.certified_msg)
            .setPositiveButtonText(R.string.phone_certified2)
            .setOnDismissListener(object : HCCommonDialog.OnDismissListener {
                override fun onDismiss(menuId: Int) {
                    if (menuId == DialogType.BUTTON_POSITIVE.ordinal) {
                        startActivityForResult(PhoneCertifiedActivity::class.java, REQUEST_CODE)
                    } else if (menuId == DialogType.BUTTON_NEGATIVE.ordinal) {
                        onBackPressed()
                    }
                }
            })
        dialog.show()

    }


}


