package com.moneyweather.view.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kakao.sdk.user.UserApiClient
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentMyBinding
import com.moneyweather.event.my.MyUiEffect
import com.moneyweather.event.my.MyUiEvent
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.model.enums.DialogType
import com.moneyweather.model.enums.SignUpType
import com.moneyweather.ui.dialog.HCCommonDialog
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.PrefRepository
import com.moneyweather.view.LoginActivity
import com.moneyweather.viewmodel.MyViewModel
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyFragment : BaseKotlinFragment<FragmentMyBinding, MyViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_my
    override val viewModel: MyViewModel by viewModels()

    override fun initStartView() {
        viewDataBinding.vm = viewModel

        initActionBar(
            viewDataBinding.iActionBar,
            R.string.bottom_nav_my,
            ActionBarLeftButtonEnum.NONE
        )

        setMenu(PrefRepository.UserInfo.isLogin)

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { data ->
                    when (data) {
                        is MyUiEffect.MyPageMenuSetting -> {
                            setMenu(isLogin = data.loginState)
                        }

                        is MyUiEffect.ShowLogoutPopup -> {
                            showLogoutPopup()
                        }
                    }
                }
            }
        }
    }

    /**
     * @param isLogin
     */
    private fun setMenu(isLogin: Boolean) {
        viewDataBinding.apply {
            if (isLogin) {
                tvLogin.visibility = View.GONE
                tvLogout.visibility = View.VISIBLE
                tvWithdraw.visibility = View.VISIBLE
            } else {
                tvLogin.visibility = View.VISIBLE
                tvLogout.visibility = View.GONE
                tvWithdraw.visibility = View.GONE
            }
        }
    }

    private fun showLogoutPopup() {
        val dialog = HCCommonDialog(requireContext())
            .setDialogType(DialogType.ALERT)
            .setLayout(R.layout.popup_logout)
            .setPositiveButtonText(R.string.logout)
            .setOnDismissListener(object : HCCommonDialog.OnDismissListener {
                override fun onDismiss(menuId: Int) {
                    if (menuId == DialogType.BUTTON_POSITIVE.ordinal) {
                        logout()

                        var loginType = when (PrefRepository.UserInfo.serviceType) {
                            SignUpType.GOOGLE.type -> {
                                LoginActivity.getGoogleLoginModule(requireContext())?.signOut()
                                "google"
                            }

                            SignUpType.NAVER.type -> {
                                NaverIdLoginSDK.logout()
                                "naver"
                            }

                            SignUpType.KAKAO.type -> {
                                UserApiClient.instance.logout {  }
                                "kakao"
                            }

                            else -> {
                                "email"
                            }
                        }

                        analyticsLogEvent(loginType)
                    }
                }
            })
        dialog.show()
    }

    private fun logout() {
        viewModel.dispatchEvent(MyUiEvent.Logout)
    }

    /**
     * @param loginType
     */
    private fun analyticsLogEvent(loginType: String) {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalyticsManager.LOGOUT, Bundle().apply {
            putString(FirebaseAnalyticsManager.LOGIN_TYPE, loginType)
        })
    }
}