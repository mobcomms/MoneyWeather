package com.moneyweather.view.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moneyweather.BuildConfig
import com.moneyweather.R
import com.moneyweather.adapter.StoreProductAdapter
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.data.remote.response.ScreenPopupsActiveResponse
import com.moneyweather.databinding.FragmentHomeBinding
import com.moneyweather.fcm.listener.setOnSingleClickListener
import com.moneyweather.model.AppInfo
import com.moneyweather.model.Weather
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.model.enums.ActionBarRightButtonEnum
import com.moneyweather.model.enums.ActivityEnum
import com.moneyweather.model.enums.DialogType
import com.moneyweather.ui.dialog.HCCommonDialog
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.CustomToast
import com.moneyweather.util.PrefRepository
import com.moneyweather.util.WeatherUtils
import com.moneyweather.util.analytics.GaBannerClickEvent
import com.moneyweather.util.analytics.GaButtonClickEvent
import com.moneyweather.view.AppWebViewActivity
import com.moneyweather.view.InviteFriendActivity
import com.moneyweather.view.LockScreenActivity
import com.moneyweather.view.LockScreenWebViewActivity
import com.moneyweather.view.LockScreenWebViewActivity.Companion.KEY_LOAD_URL
import com.moneyweather.view.LockScreenWebViewActivity.Companion.KEY_VIEW_TYPE
import com.moneyweather.view.LoginActivity
import com.moneyweather.view.MainActivity
import com.moneyweather.view.MainActivity.Companion.TAB_STORE
import com.moneyweather.view.SettingActivity
import com.moneyweather.view.ThemeSettingActivity
import com.moneyweather.viewmodel.MainHomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Runnable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MainHomeFragment : BaseKotlinFragment<FragmentHomeBinding, MainHomeViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_home
    override val viewModel: MainHomeViewModel by viewModels()

    private var mStoreProductAdapter: StoreProductAdapter? = null

    private var screenPopupData: ScreenPopupsActiveResponse.Data? = null
    private var instanceBundle: Bundle? = null
    private var isLightPopupCalled : Boolean = false
    override fun initStartView() {
        viewDataBinding.vm = viewModel
        initActionBar(viewDataBinding.iActionBar, R.string.bottom_nav_home, ActionBarLeftButtonEnum.THEME, ActionBarRightButtonEnum.SETTINGS)

        viewModel.resultProductList.observe(this, Observer {
            //updateCount(productListBody.count)

            mStoreProductAdapter?.addData(it)
        })

        viewModel.user?.observe(viewLifecycleOwner) {
            if (it.name == getString(R.string.user_type_guest)) {
                viewDataBinding.iconUser.visibility = View.GONE
                viewDataBinding.userId.visibility = View.GONE
                viewDataBinding.loginBtn.visibility = View.VISIBLE
            } else {
                viewDataBinding.iconUser.visibility = View.VISIBLE
                viewDataBinding.userId.visibility = View.VISIBLE
                viewDataBinding.loginBtn.visibility = View.GONE
            }
        }

        viewModel.resultScreenPopupsActive?.observe(this, Observer {
            it?.let {
                if (it != null && it.isExist!!) {
                    screenPopupData = it
                    popupNotice(it.imageUrl!!)
                }
            }
        })

        viewModel.resultNoticeDetail?.observe(this, Observer {
            it?.let {
                if (it != null) {
                    startActivity(
                        Intent(requireContext(), SettingActivity::class.java)
                            .putExtra("move_activity", ActivityEnum.NOTICE_DETAIL)
                            .putExtra("title", it.title)
                            .putExtra("content", it.content)
                            .putExtra("createdAt", it.createdAt)
                    )
                }
            }
        })

        AppInfo.currentWeatherInfo?.observe(this, Observer {
            it?.let {
                setSunrise(it)
            }
        })

        if ( !isLightPopupCalled ) {
            isLightPopupCalled = true
            val daroLightPopup = viewModel.getDaroLightPopup(activity)

            if ( daroLightPopup != null ) {
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    viewDataBinding.mainDaroContainer.visibility = View.GONE
                }, 8000)
                viewDataBinding.mainDaroContainer.removeAllViews()
                viewDataBinding.mainDaroContainer.addView(daroLightPopup)
            }
        }

        val daroMrec = viewModel.getDaroMrec(activity)
        if ( daroMrec != null ) {
            viewDataBinding.mainDaroMrecContainer.removeAllViews()
            viewDataBinding.mainDaroMrecContainer.addView(daroMrec)
        }

        viewDataBinding.apply {

            recyclerProduct.layoutManager = LinearLayoutManager(
                context,
                RecyclerView.HORIZONTAL, false
            )
            mStoreProductAdapter = StoreProductAdapter(context = requireContext(), isHorizontal = true)
            recyclerProduct.adapter = mStoreProductAdapter

            ivPomissionZoneButton.setOnSingleClickListener {
                if (BuildConfig.DEBUG) {
                    CustomToast.showToast(requireContext(), R.string.message_on_debug_mode)
                    return@setOnSingleClickListener
                }

                val pomissionZoneUrl = PrefRepository.LockQuickInfo.pomissionZoneUrl
                if (pomissionZoneUrl.isEmpty()) return@setOnSingleClickListener

                // pomission zone을 웹뷰로 노출
                val pomissionZoneIntent = Intent(requireActivity(), AppWebViewActivity::class.java).apply {
                    putExtra(KEY_LOAD_URL, pomissionZoneUrl)
                    putExtra(KEY_VIEW_TYPE, LockScreenWebViewActivity.ViewType.POMISSION_ZONE.name)
                    putExtra(LockScreenActivity.EXTRA_IS_LOCK_SCREEN, false)
                }
                startActivity(pomissionZoneIntent)

                // Ga Log Event
                GaBannerClickEvent.logHomeBannerPomissionClickEvent()
            }

            inviteBanner.setOnClickListener {
                if (!PrefRepository.UserInfo.isLogin)
                    CustomToast.showToast(requireContext(), R.string.message_non_login_user)
                else {
                    startActivityForResult(InviteFriendActivity::class.java, REQUEST_CODE)

                    // Ga Log Event
                    GaBannerClickEvent.logInviteBannerHomeClickEvent()
                }
            }

            info.setOnClickListener {
                popupInfo()
            }
            npay.setOnClickListener {
                val activity = activity as MainActivity?
                activity?.moveTab(TAB_STORE)

                // Ga Log Event
                GaButtonClickEvent.logHomeShopMoreButtonClickEvent()
            }
            btnQuiz.setOnClickListener {

            }
            loginBtn.setOnClickListener {
                startActivity(Intent(context, LoginActivity::class.java))
            }

            btnAccumulate.setOnClickListener {
//                try {
//                    val activity = activity as MainActivity?
//                    activity?.moveTab(POS_PAGE_CHARGE)
//
//                    // Ga Log Event
//                    GaButtonClickEvent.logHomeOfferwallButtonClickEvent()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
            }
        }

        viewModel.connectSearchProduct("", "", "", 1, true)
        viewModel.connectConfigMy()

        val shouldShowContent = CommonUtils.shouldShowContent(
            PrefRepository.SettingInfo.checkNoticePopup
        )
        if (shouldShowContent) {
            viewModel.connectScreenPopupsActive()
        }


    }

    private fun popupInfo() {
        val dialog: HCCommonDialog = HCCommonDialog(requireContext())
            .setDialogType(DialogType.CONFIRM)
            .setMainPointPolicyWebViewLayout(
                url = resources.getString(R.string.main_point_policy_info),
                height = resources.getDimensionPixelSize(R.dimen.main_point_policy_info_height)
            )
            .setPositiveButtonText(R.string.confirm)
        dialog.show()

    }

    private fun popupNotice(imgUrl: String) {
        val dialog: HCCommonDialog = HCCommonDialog(requireContext())
            .setDialogType(DialogType.ALERT)
            .setNoticeDialogImage(imgUrl)
            .setPositiveButtonText(R.string.popup_notice_right_button_text)
            .setNegativeButtonText(getString(R.string.popup_notice_left_button_text))
            .setOnDismissListener(object : HCCommonDialog.OnDismissListener {
                override fun onDismiss(menuId: Int) {
                    when (menuId) {
                        DialogType.BUTTON_POSITIVE.ordinal -> {
                            screenPopupData?.let {
                                viewModel.connectNoticeDetail(it.linkedNoticeId!!)
                            }
                        }

                        DialogType.BUTTON_NEGATIVE.ordinal -> {
                            PrefRepository.SettingInfo.checkNoticePopup = CommonUtils.getCurrentDate()
                        }
                    }
                }
            })
        dialog.show()
    }

    override fun onChildResume() {
        super.onChildResume()
    }

    override fun onClickActionBarLeftButton() {
        super.onClickActionBarLeftButton()

        startActivity(ThemeSettingActivity::class.java)

    }

    override fun onClickActionBarRightButton(rightSecondButtonEnum: ActionBarRightButtonEnum) {
        super.onClickActionBarRightButton(rightSecondButtonEnum)
        startActivity(SettingActivity::class.java)

    }

    /**
     * 일출-일몰 설정
     * @param weather
     */
    private fun setSunrise(weather: Weather) {
        var background = resources.getDrawable(R.drawable.gradient_background_sunset, null)
        var title = resources.getString(R.string.progressbar_sunrise_sunset_title3)
        var color = resources.getColor(R.color.sunset_progressbar_title_color, null)
        var image = R.drawable.icon_sunset
        var startTime = StringBuilder().append(resources.getString(R.string.sunset)).append(" ").append(weather.sunset())
        var endTime = StringBuilder().append(resources.getString(R.string.sunrise)).append(" ").append(weather.tomorrowSunrise())

        val isSunrise = WeatherUtils.checkSunrise(weather)
        if (isSunrise) {
            background = resources.getDrawable(R.drawable.gradient_background_sunrise, null)
            title = when (weather.skyCode) {
                1, 2, 16, 17, 18, 19, 20, 21 ->
                    resources.getString(R.string.progressbar_sunrise_sunset_title1)

                else ->
                    resources.getString(R.string.progressbar_sunrise_sunset_title2)
            }
            color = resources.getColor(R.color.background_theme_status_bar_color, null)
            image = R.drawable.icon_weather_1
            startTime = StringBuilder().append(resources.getString(R.string.sunrise)).append(" ").append(weather.sunrise())
            endTime = StringBuilder().append(resources.getString(R.string.sunset)).append(" ").append(weather.sunset())
        }

        viewDataBinding.apply {
            sunriseSunsetLayout.background = background

            tvProgressbarTitle.text = title
            tvProgressbarTitle.setTextColor(color)

            progressBar.setImage(image)
            progressBar.setProgress(calculateProgress(weather, isSunrise))

            tvSunrise.text = startTime
            tvSunset.text = endTime
        }
    }

    /**
     * 일출-일몰 현재 진행률 반환
     * @param weather
     * @param isSunrise
     * @return
     */
    private fun calculateProgress(weather: Weather, isSunrise: Boolean): Float {
        val sunriseTime = weather.sunrise?.toInt()
        val sunsetTime = weather.sunset?.toInt()
        val tomorrowSunriseTime = weather.tomorrowSunrise?.toInt()
        val currentTime = SimpleDateFormat("HHmm", Locale.getDefault()).format(Date()).toInt()
        val defaultTime = 2400

        return if (isSunrise) {
            // 일출 ~ 일몰
            (currentTime - sunriseTime!!) / (sunsetTime!! - sunriseTime).toFloat()
        } else {
            if (currentTime > sunsetTime!!) {
                // 일몰 ~ 24:00
                (currentTime - sunsetTime) / (defaultTime - sunsetTime + tomorrowSunriseTime!!).toFloat()
            } else {
                // 00:00 ~ 다음날 일출
                (defaultTime - sunsetTime + currentTime) / (defaultTime - sunsetTime + tomorrowSunriseTime!!).toFloat()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (RESULT_OK == resultCode && REQUEST_CODE == requestCode) {
            var isRedeemed = data?.getBooleanExtra("isRedeemed", false)
            if (isRedeemed!!) {
                viewModel.connectUserPoint()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val SUCCESS_GAME = "Success"
        const val REQUEST_CODE = 1000
    }


}