package com.moneyweather.view.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.moneyweather.R
import com.moneyweather.adapter.StoreProductAdapter
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentShopBinding
import com.moneyweather.model.ShopCategoryItem
import com.moneyweather.ui.WrapContentLinearLayoutManager
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.PrefRepository
import com.moneyweather.viewmodel.ShopViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopFragment : BaseKotlinFragment<FragmentShopBinding, ShopViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_shop
    override val viewModel: ShopViewModel by viewModels()

    private val REQUEST_CODE_COUPON = 1111

    private var chooseDialogAdapter: ChooseDialogAdapter? = null
    private var mStoreProductAdapter: StoreProductAdapter? = null

    private var mCategoryList: List<ShopCategoryItem>? = null

    private var isNpayShop = false


    override fun initStartView() {
        viewDataBinding.vm = viewModel
        var bundle = arguments

        bundle?.let {
            isNpayShop = it.getBoolean("isNpay")

        }

        if (isNpayShop) {
            viewDataBinding.topTab.visibility = View.GONE

            val daroShopBannerView = viewModel.getDaroShopBanner(activity)
            if ( daroShopBannerView != null ) {
                viewDataBinding.shopDaroMainBanner.visibility = View.VISIBLE
                viewDataBinding.shopDaroMainBanner.removeAllViews()
                viewDataBinding.shopDaroMainBanner.addView(daroShopBannerView)
            } else
                viewDataBinding.shopDaroMainBanner.visibility = View.GONE
        } else {
            viewDataBinding.shopDaroMainBanner.visibility = View.GONE
            viewModel.resultCategoryList.observe(this, Observer {
                mCategoryList = it

                mCategoryList.apply {
                    if (it.isNotEmpty()) {
                        viewDataBinding.layTab.apply {
                            removeAllTabs()
                            for (category in it) {
                                val tab = newTab()
                                tab.text = category.category
                                addTab(tab)
                            }
                            tag = "inited"
                            post { scrollTo(0, 0) }
                        }
                        searchProduct(1)
                    }
                }
            })
        }
        viewModel.resultProductList.observe(this, Observer {
            //updateCount(productListBody.count)

            mStoreProductAdapter?.addData(it)
        })

        initView()


    }

    private fun initView() {

        viewDataBinding.apply {

            mStoreProductAdapter = StoreProductAdapter(context = requireContext(), storeFragment = this@ShopFragment, isHorizontal = false)

            recyclerProduct.apply {
                layoutManager = LinearLayoutManager(context)
                itemAnimator = null
                adapter = mStoreProductAdapter
            }

            layTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (layTab.tag == "inited") {
                        chooseDialogAdapter = null

                        initSearchCondition()
                        searchProduct(1)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

            })

            editSearch.setOnKeyListener { v, keyCode, event ->

                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {

                    editSearch.applicationWindowToken?.let { applicationWindowToken ->
                        val inputService =
                            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputService.hideSoftInputFromWindow(
                            applicationWindowToken,
                            InputMethodManager.HIDE_NOT_ALWAYS
                        )
                    }

                    searchProduct(1)

                }

                return@setOnKeyListener false
            }

            btnSortType.setOnClickListener {
                popupChooseStore()
            }

        }
    }


    private fun popupChooseStore() {
        var builder2: AlertDialog.Builder? = null
        builder2 =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) AlertDialog.Builder(context) else AlertDialog.Builder(
                context,
                android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
            )
        val dialogView = layoutInflater.inflate(R.layout.say_write_popup, null)
        builder2.setView(dialogView)
        builder2.setCustomTitle(null)
        val mDialog = builder2.create()
        mDialog.getWindow()?.setGravity(Gravity.CENTER)
        mDialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val mTitle = dialogView.findViewById<TextView>(R.id.mTitle)
        mTitle.text = "교환처선택"
        val mListView = dialogView.findViewById<RecyclerView>(R.id.list)

        if (chooseDialogAdapter == null) {
            chooseDialogAdapter = ChooseDialogAdapter(getAffiliateList(), this)
        }
        chooseDialogAdapter?.dialog = mDialog

        mListView.setAdapter(chooseDialogAdapter)
        mListView.setLayoutManager(WrapContentLinearLayoutManager(context))
        dialogView.findViewById<View>(R.id.close).setOnClickListener {
            mDialog.dismiss()
        }
        mDialog.show()
    }

    fun getAffiliateList(): List<String> {
        return mCategoryList?.get(viewDataBinding.layTab.selectedTabPosition ?: 0)?.affiliates
            ?: listOf()

    }

    private fun initData() {
        viewDataBinding.let {

            if (!isNpayShop)
                viewModel.connectCategoryList()
            else {
                viewModel.connectSearchProduct(
                    "",
                    "",
                    "",
                    1,
                    isNpayShop
                )
            }

            if (PrefRepository.UserInfo.isVerification) {
                updateCouponState()
            }
        }

    }

    fun initSearchCondition() {
        viewDataBinding.apply {
            editAffiliate.text = ""
            editSearch.setText("")
        }
    }

    class ChooseDialogAdapter internal constructor(
        val list: List<String>,
        val storeFragment: ShopFragment
    ) : RecyclerView.Adapter<ChooseDialogAdapter.ViewHolder>() {

        var dialog: AlertDialog? = null

        private var selectPosition = -1

        // 아이템 뷰를 저장하는 뷰홀더 클래스.
        inner class ViewHolder internal constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var mRadioButton: RadioButton

            init {
                mRadioButton = itemView.findViewById(R.id.radio_btn)
                mRadioButton.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(view: View) {
                        selectPosition = adapterPosition
                        storeFragment.settingAffiliate(list.get(adapterPosition)!!)
                        dialog?.dismiss()
                    }
                })
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val context = parent.context
            val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.say_popup_select_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list.get(position)
            holder.mRadioButton.text = item
            holder.mRadioButton.setTypeface(
                null,
                if (selectPosition == position) Typeface.BOLD else Typeface.NORMAL
            )
            holder.mRadioButton.isChecked = selectPosition == position
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }

    fun settingAffiliate(affiliate: String) {
        viewDataBinding.apply {
            editAffiliate.text = affiliate
            editSearch.setText("")
            searchProduct(1)
        }
    }

    fun searchProduct(page: Int) {

        if (page == 1) {
            updateCount(0)
            mStoreProductAdapter?.initData()
        }
        mCategoryList?.let { mCategoryList ->

            val categoryName = with(viewDataBinding.layTab.selectedTabPosition) {
                if (this > 0) {
                    mCategoryList.get(this).category
                } else ""
            }
            viewModel.connectSearchProduct(
                categoryName!!,
                viewDataBinding.editAffiliate.text.toString(),
                viewDataBinding.editSearch.text.toString(),
                page,
                isNpayShop
            )

        }

    }

    private fun updateCouponState() {
        //  viewModel.connectUppdateCouponState()
    }

    fun nextProduct(page: Int) {

    }

    private fun updateCount(totalCount: Int) {
        val totalCountStr = String.format(
            getString(R.string.total_count),
            CommonUtils.getCommaNumeric(totalCount.toFloat())
        )
        viewDataBinding.txtProductCount.setText(totalCountStr)
    }

    override fun onResume() {
        super.onResume()
        viewDataBinding.editSearch.setText("")
        initData()
    }
}