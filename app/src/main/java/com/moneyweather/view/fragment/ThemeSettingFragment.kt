package com.moneyweather.view.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.adapter.ThemeSettingAdapter
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentThemeSettingBinding
import com.moneyweather.model.CarouselCard
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.model.enums.ActionBarRightButtonEnum
import com.moneyweather.ui.carouselview.Carousel
import com.moneyweather.ui.carouselview.CarouselLazyLoadListener
import com.moneyweather.ui.carouselview.CarouselListener
import com.moneyweather.ui.carouselview.CarouselView
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.PrefRepository
import com.moneyweather.view.LoginActivity
import com.moneyweather.view.SettingActivity
import com.moneyweather.view.ThemeSettingActivity
import com.moneyweather.viewmodel.ThemeSettingViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ThemeSettingFragment : BaseKotlinFragment<FragmentThemeSettingBinding, ThemeSettingViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_theme_setting
    override val viewModel: ThemeSettingViewModel by viewModels()

    private var currentPosition: Int = 0
    private val ivArrayDotsPager = arrayOfNulls<ImageView>(5)
    private lateinit var carousel: Carousel

    private var type: String? = ""

    override fun initStartView() {
        viewDataBinding.vm = viewModel

        arguments?.let { argument ->
            type = argument.getString("type")
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            this@ThemeSettingFragment, onBackPressedCallback
        )

        val screenName = if (PrefRepository.UserInfo.isFirst) {
            initActionBar(viewDataBinding.iActionBar, R.string.theme_choice, ActionBarRightButtonEnum.NEXT)
            "테마 선택(앱 최초 설치시)"
        } else {
            initActionBar(viewDataBinding.iActionBar, R.string.theme, ActionBarLeftButtonEnum.BACK_BUTTON)
            "테마 설정"
        }

        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, screenName)
        })

        currentPosition = PrefRepository.SettingInfo.selectThemeType


        viewDataBinding.apply {
            val adapter = ThemeSettingAdapter()
            setupPagerIndicatorDots(indicatorTabLayout)

            activity?.let {

                carousel = Carousel(it, carouselView, adapter)
                carousel.setOrientation(CarouselView.HORIZONTAL, false)
                carousel.scaleView(true)
                carousel.setCurrentPosition(currentPosition)

                carousel.lazyLoad(true, object : CarouselLazyLoadListener {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, view: CarouselView) {

                    }
                })
                adapter.setOnClickListener(object :
                    ThemeSettingAdapter.OnClick {
                    override fun click(model: CarouselCard) {

                    }
                })

                carousel.addCarouselListener(object : CarouselListener {
                    override fun onPositionChange(position: Int) {

                        for (i in 0 until ivArrayDotsPager.size) {
                            ivArrayDotsPager[i]?.setImageResource(R.drawable.default_dot)
                        }
                        ivArrayDotsPager[position]?.setImageResource(R.drawable.selected_dot)

                        currentPosition = position

                        when (position) {
                            0 -> {
                                themeSubject.text = getString(R.string.theme_subject_info)
                                themeDesc.text = getString(R.string.theme_desc_info)

                            }

                            1 -> {
                                themeSubject.text = getString(R.string.theme_subject_simple)
                                themeDesc.text = getString(R.string.theme_desc_simple)
                            }

                            2 -> {
                                themeSubject.text = getString(R.string.theme_subject_calendar)
                                themeDesc.text = getString(R.string.theme_desc_calendar)
                            }

                            3 -> {
                                themeSubject.text = getString(R.string.theme_subject_background)
                                themeDesc.text = getString(R.string.theme_desc_background)
                            }

                            4 -> {
                                themeSubject.text = getString(R.string.theme_subject_video)
                                themeDesc.text = getString(R.string.theme_desc_video)
                            }
                        }

                    }

                    override fun onScroll(dx: Int, dy: Int) {
                    }
                })

                carousel.add(CarouselCard(0))
                carousel.add(CarouselCard(1))
                carousel.add(CarouselCard(2))
                carousel.add(CarouselCard(3))
                carousel.add(CarouselCard(4))
            }

            btnComplete.setOnClickListener {
                PrefRepository.SettingInfo.selectThemeType = currentPosition
                carousel?.let { it.adapter.notifyDataSetChanged() }
                viewModel.connectConfigMy()
            }
        }

        viewModel.connectConfigMy()
    }

    private fun finish() {
        try {
            var intent = Intent()
            intent.putExtra("isThemeChange", true)
            requireActivity().setResult(RESULT_OK, intent)

            when (activity) {
                is SettingActivity -> {
                    finishFragment()
                    type?.let {
                        if (it.isNotEmpty() && it == "lockscreen") {
                            activity?.finish()
                        }
                    }
                }

                is ThemeSettingActivity -> activity?.finish()
            }
        } catch (e: Exception) {
            activity?.finish()
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }

    override fun onClickActionBarRightButton(rightSecondButtonEnum: ActionBarRightButtonEnum) {
        super.onClickActionBarRightButton(rightSecondButtonEnum)

        startActivity(Intent(context, LoginActivity::class.java))
        activity?.finish()
    }

    override fun onClickActionBarLeftButton() {
        finish()
    }

    private fun setupPagerIndicatorDots(indicatorTabLayout: ViewGroup) {

        for (i in ivArrayDotsPager.indices) {
            ivArrayDotsPager[i] = ImageView(requireActivity())
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(5, 0, 5, 0)
            ivArrayDotsPager[i]?.layoutParams = params
            ivArrayDotsPager[i]?.setOnClickListener {
                it.alpha = 1f
            }
            indicatorTabLayout.addView(ivArrayDotsPager[i])
            indicatorTabLayout.bringToFront()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

    }

    override fun onChildResume() {
        super.onChildResume()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult is call")
        super.onActivityResult(requestCode, resultCode, data)
        carousel?.let {
            it.adapter.notifyDataSetChanged()
        }

        viewModel.connectConfigMy()
    }

}