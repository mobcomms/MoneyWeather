package com.moneyweather.view

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.datamanagersdk.ENDataManager
import com.enliple.datamanagersdk.events.models.ENOrder
import com.enliple.datamanagersdk.events.models.ENViewedProduct
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.adapter.ProductDetailAdapter
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.data.remote.response.AuthResponse
import com.moneyweather.databinding.ActivityProductDetailBinding
import com.moneyweather.model.ProductItem
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.model.enums.DialogType
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.model.enums.TermsType
import com.moneyweather.ui.dialog.HCCommonDialog
import com.moneyweather.ui.dialog.HCCommonRoundBtnDialog
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.CustomToast
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.Logger
import com.moneyweather.util.PrefRepository
import com.moneyweather.viewmodel.ProductDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ProductDetailActivity :
    BaseKotlinActivity<ActivityProductDetailBinding, ProductDetailViewModel>(),
    View.OnClickListener {

    override val layoutResourceId: Int get() = R.layout.activity_product_detail
    override val viewModel: ProductDetailViewModel by viewModels()

    private var RESULT_CODE: Int = 8888
    private var productDetailAdapter: ProductDetailAdapter? = null
    private var mProductVO: ProductItem? = null

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "상품 상세")
        })

        viewDataBinding.vm = viewModel

        initActionBar(viewDataBinding.iActionBar, R.string.store_detail, ActionBarLeftButtonEnum.BACK_BUTTON)
        initView()
        loadData()

        viewModel.resultVerification.observe(this, androidx.lifecycle.Observer {
            when (it) {
                true -> {
                    purchase()
                }

                false -> {
                    verificationPopup()
                }
            }

        })

        viewModel.resultPurchaseCoupon.observe(this, androidx.lifecycle.Observer {
            when (it.result) {
                ResultCode.SUCCESS.resultCode -> {
                    Toast.makeText(
                        applicationContext,
                        "성공적으로 쿠폰을 구매했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }

                ResultCode.COUPON_DAILY_200000_LIMIT.resultCode -> {
                    val dialog = HCCommonDialog(this@ProductDetailActivity)
                        .setDialogType(DialogType.CONFIRM)
                        .setDialogTitle("쿠폰구매제한")
                        .setContent(it.msg)
                    dialog.show()
                }

                else -> {

                }
            }

        })





        viewModel.resultProduct.observe(this, androidx.lifecycle.Observer {
            mProductVO = it

            try {
                if (ENDataManager.isInitialized()) {
                    val viewedProduct = ENViewedProduct()
                    viewedProduct.setProductId(it.goodsId)
                    viewedProduct.setProductName(it.goodsName)
                    viewedProduct.setImageUrl(it.goodsImageSmall)

                    it.salePrice?.let { salePrice ->
                        if (salePrice.isNotEmpty()) {
                            viewedProduct.setPrice(salePrice.toInt())
                        }
                    }

                    ENDataManager.getInstance().addEvent(viewedProduct)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            viewDataBinding.apply {
                Glide.with(this@ProductDetailActivity)
                    .load(it.goodsImageSmall)
                    .centerInside()
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(imgProduct)

                txtPlaceForSale.text = it.affiliate
                txtProductName.text = it.goodsName

                val totalPriceStr = String.format(
                    getString(R.string.add_point),
                    CommonUtils.getCommaNumeric(it.salePrice!!.toFloat())
                )
                txtProductPrice.text = totalPriceStr


                val limitDateStr = String.format(
                    getString(R.string.due_date_value),
                    CommonUtils.getCommaNumeric(it.limitDay!!.toFloat())
                )
                txtDueDate.text = limitDateStr

                productDetailAdapter?.updateData(mProductVO)
                requestLayoutPagerHeight(0)
            }

        })
    }

    override fun onClick(view: View?) {

        if (view?.id == R.id.btnPurchase) {
            purchaseDialog()
        } else if (view?.id == R.id.btnTerms) {
            val policyIntent = Intent(this, TermsActivity::class.java)
            policyIntent.putExtra("type", TermsType.SERVICE)
            startActivity(policyIntent)
        } else if (view?.id == R.id.btnBusinessInfo) {
            val policyIntent = Intent(this, TermsActivity::class.java)
            policyIntent.putExtra("type", TermsType.BUSINESS_INFO)
            startActivity(policyIntent)
        }
    }

    private fun initView() {
        viewDataBinding.apply {
            btnTerms.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            btnBusinessInfo.paintFlags = Paint.UNDERLINE_TEXT_FLAG

            productDetailAdapter = ProductDetailAdapter(this@ProductDetailActivity)
            pagerDetail.setAdapter(productDetailAdapter)

            layTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (layTab.selectedTabPosition != pagerDetail.currentItem) {
                        pagerDetail.currentItem = layTab.selectedTabPosition
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

            })

            pagerDetail.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    layTab.getTabAt(position)?.select()

                    requestLayoutPagerHeight(position)
                }
            })

            TabLayoutMediator(layTab, pagerDetail) { tab: TabLayout.Tab, position: Int ->
                if (position == 0) {
                    tab.setText(R.string.product_description)
                } else {
                    tab.setText(R.string.product_precautions)
                }
            }.attach()
        }

    }

    private fun requestLayoutPagerHeight(position: Int) {
        viewDataBinding.apply {
            pagerDetail.post {
                val view =
                    (pagerDetail.getChildAt(0) as RecyclerView).layoutManager!!.findViewByPosition(
                        position
                    ) as ConstraintLayout?
                view?.let { view ->
                    updatePagerHeight(view.getChildAt(0))
                }
            }
        }
    }

    private fun updatePagerHeight(view: View) {

        val wMeasureSpec = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
        val hMeasureSpec =
            View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.UNSPECIFIED)
        view.measure(wMeasureSpec, hMeasureSpec)

        val measuredHeight = view.measuredHeight
        Logger.d("measuredHeight : $measuredHeight")

        viewDataBinding.apply {
            pagerDetail.layoutParams?.height = measuredHeight
            pagerDetail.requestLayout()
        }
    }

    private fun purchaseDialog() {

        if (PrefRepository.UserInfo.isLogin) {
            val dialog = HCCommonDialog(this)
                .setDialogType(DialogType.ALERT)
                .setDialogTitle(R.string.purchase1)
                .setContent(R.string.product_purchase_confirm_dialog_content)
                .setPositiveButtonText(R.string.ok)
                .setNegativeButtonText(R.string.cancel)
                .setOnDismissListener(object : HCCommonDialog.OnDismissListener {
                    override fun onDismiss(menuId: Int) {
                        if (menuId == DialogType.BUTTON_POSITIVE.ordinal) {
                            checkAuth() { resultCode, authVO ->
                                when (resultCode) {
                                    ResultCode.SUCCESS -> {
                                        purchase()
                                    }

                                    ResultCode.NO_AUTH -> {
                                        //본인인증

                                    }

                                    ResultCode.DUPLICATE_AUTH -> {
                                        //본인인증 중복 x 노티
                                        if (authVO != null)
                                            showDuplicatedUserAuthDialog(authVO.msg, authVO.email)
                                    }

                                    else -> {}
                                }
                            }

                        }
                    }
                })
            dialog.show()
        } else {
            CustomToast.showToast(applicationContext, R.string.message_non_login_user)
        }

    }


    private fun showDuplicatedUserAuthDialog(msg: String, email: String) {
        val depositorDialog = HCCommonRoundBtnDialog(this)
            .setDialogTitle(R.string.phone_certified)
            .setContent(msg, email)
            .setConfirmButtonText(getString(R.string.ok))
            .setOnDismissListener(object : HCCommonRoundBtnDialog.OnDismissListener {
                override fun onDismiss(menuId: Int) {
                }
            })
        depositorDialog.show()
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
                        startActivityForResult(PhoneCertifiedActivity::class.java, RESULT_CODE)
                    }
                }
            })
        dialog.show()

    }

    private fun checkAuth(onResult: (ResultCode, AuthResponse?) -> Unit) {
//        var authPhone = PrefRepository.UserInfo.auth_phone
//
//        authPhone = authPhone.replace("-".toRegex(), "")

        //  viewModel.connectGetAuth(authPhone)

        viewModel.connectVerification()
    }

    private fun purchase() {

        mProductVO?.let { mProductVO ->
            val param = HashMap<String, Any?>()
            param.put("goodsId", mProductVO.goodsId)
            viewDataBinding.apply {
                btnPurchase.isClickable = false
                viewModel.connectPurchaseCoupon(param)
            }

            try {
                if (ENDataManager.isInitialized()) {
                    val order = ENOrder()
                    order.setOrderId(mProductVO.goodsId)

                    if (PrefRepository.UserInfo.userId.isNotEmpty()) {
                        order.setMemberId(PrefRepository.UserInfo.userId)
                    }

                    if (PrefRepository.UserInfo.email.isNotEmpty()) {
                        order.setEmail(PrefRepository.UserInfo.email)
                    }

                    mProductVO.salePrice?.let { salePrice ->
                        if (salePrice.isNotEmpty()) {
                            order.setTotalPrice(salePrice.toInt())
                        }
                    }

                    ENDataManager.getInstance().addEvent(order)
                } else {

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendLog() {
        try {

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadData() {
        val productPk = intent.extras?.getString("productPk")
        productPk?.let { productPk ->
            viewModel.connectGetProductDetail(productPk)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult is call")
        super.onActivityResult(requestCode, resultCode, data)

    }
}