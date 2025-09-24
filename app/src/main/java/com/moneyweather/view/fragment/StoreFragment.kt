package com.moneyweather.view.fragment

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.moneyweather.R
import com.moneyweather.base.BaseFragment
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.base.BaseViewPager2Adapter
import com.moneyweather.databinding.FragmentStoreBinding
import com.moneyweather.event.store.StoreUiEvent
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.model.enums.ActionBarRightButtonEnum
import com.moneyweather.model.enums.DialogType
import com.moneyweather.ui.dialog.HCCommonDialog
import com.moneyweather.util.CustomToast
import com.moneyweather.util.PrefRepository
import com.moneyweather.view.MyCouponActivity
import com.moneyweather.viewmodel.StoreViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoreFragment : BaseKotlinFragment<FragmentStoreBinding, StoreViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_store
    override val viewModel: StoreViewModel by viewModels()

    override fun initStartView() {
        viewDataBinding.vm = viewModel

        var bundle = arguments
        val isMyRun = bundle?.getBoolean("my", false)

        if (isMyRun == true) // 나의 포인트에서 넘어옴
            initActionBar(viewDataBinding.iActionBar, R.string.bottom_nav_store, ActionBarLeftButtonEnum.BACK_BUTTON, ActionBarRightButtonEnum.COUPON)
        else
            initActionBar(viewDataBinding.iActionBar, R.string.bottom_nav_store, ActionBarRightButtonEnum.COUPON)

        viewDataBinding.apply {
            val npayShop = ShopFragment().apply {
                arguments = Bundle().apply { putBoolean("isNpay", true) }
            }
            val shopFragment = ShopFragment()
            val fragments = arrayListOf<BaseFragment>(npayShop, shopFragment)
            val tabNames = listOf(getString(R.string.npay_shop), getString(R.string.coupon_shop))

            viewPager2.adapter = BaseViewPager2Adapter(this@StoreFragment, fragments, tabNames)
            viewPager2.offscreenPageLimit = tabNames.size
            viewPager2.isUserInputEnabled = false

            tvEnablePointTitle.setOnClickListener { popupAvailablePoint() }
        }
    }

    override fun onClickActionBarRightButton(rightSecondButtonEnum: ActionBarRightButtonEnum) {
        super.onClickActionBarRightButton(rightSecondButtonEnum)
        if (PrefRepository.UserInfo.isLogin) {
            startActivity(MyCouponActivity::class.java)
        } else {
            CustomToast.showToast(requireContext(), R.string.message_non_login_user)
        }

    }

    /**
     * 사용 가능한 포인트 안내
     */
    private fun popupAvailablePoint() {
        val dialog = HCCommonDialog(requireContext())
            .setDialogType(DialogType.CONFIRM)
            .setLayout(R.layout.popup_available_point_info)
            .setPositiveButtonText(R.string.confirm)
        dialog.show()
    }

    override fun onResume() {
        super.onResume()

        viewModel.dispatchEvent(StoreUiEvent.FetchAvailablePoints)
    }
}