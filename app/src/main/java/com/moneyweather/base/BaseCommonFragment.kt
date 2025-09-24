package com.moneyweather.base

import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import com.google.gson.Gson
import com.moneyweather.R
import com.moneyweather.databinding.ViewCommonActionBarBinding
import com.moneyweather.listener.ActionBarButtonEventListener
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.model.enums.ActionBarRightButtonEnum
import com.moneyweather.model.enums.ActionBarRightSecondButtonEnum

open class BaseCommonFragment : BaseFragment(), ActionBarButtonEventListener {
    private var TAG = this::class.java.simpleName
    protected val INTERPOLATOR: Interpolator = DecelerateInterpolator()

    var searchKeywordHint: String? = null

    var viewBinding: ViewCommonActionBarBinding? = null

    override fun onSearchStart(keyword: String) {
    }

    override fun onSearchClose() {
    }

    override fun onClickActionBarLeftButton() {
    }

    override fun onClickActionBarRightButton(rightSecondButtonEnum: ActionBarRightButtonEnum) {
        when (rightSecondButtonEnum) {
            ActionBarRightButtonEnum.SETTINGS -> {

            }

            else -> {
            }
        }
    }

    override fun onClickActionBarRightSecondButton(rightSecondSecondButtonEnum: ActionBarRightSecondButtonEnum) {
        when (rightSecondSecondButtonEnum) {
            ActionBarRightSecondButtonEnum.SEARCH -> {

            }

            else -> {
            }
        }
    }

    fun startActivity(c: Class<*>?, any: Any) {
        startActivity(Gson().toJson(any), c)
    }

    override fun onClickActionBarTitle() {
    }

    protected fun initActionBarName(strId: Int) {
        initActionBarName(getString(strId))
    }

    protected fun initActionBarName(str: String) {
        viewBinding?.apply {
            if (tvActionBarName != null) {
                tvActionBarName.text = str
                tvActionBarName.setOnClickListener { onClickActionBarTitle() }
            }
        }


    }

    protected fun initActionBarSearchKeywordHint(str: Int) {
        initActionBarSearchKeywordHint(getString(str))
    }

    protected fun initActionBarSearchKeywordHint(str: String) {
        searchKeywordHint = str
    }

    protected fun initActionBarOnlyLeftBackType(title: Int?) {
        title?.let { initActionBarOnlyLeftBackType(getString(it)) }
    }

    protected fun initActionBarOnlyLeftBackType(title: String?) {
        initActionBar(title, ActionBarLeftButtonEnum.BACK_BUTTON, ActionBarRightButtonEnum.NONE)
    }

    protected fun initActionBar(viewDataBinding: ViewCommonActionBarBinding, title: Int?, leftButtonEnum: ActionBarLeftButtonEnum) {
        viewBinding = viewDataBinding
        title?.let { initActionBar(getString(it), leftButtonEnum, ActionBarRightButtonEnum.NONE) }
    }

    protected fun initActionBar(viewDataBinding: ViewCommonActionBarBinding, title: Int?, rightButtonEnum: ActionBarRightButtonEnum) {
        viewBinding = viewDataBinding
        title?.let { initActionBar(getString(it), ActionBarLeftButtonEnum.NONE, rightButtonEnum) }
    }

    protected fun initActionBar(
        viewDataBinding: ViewCommonActionBarBinding,
        title: Int?,
        leftButtonEnum: ActionBarLeftButtonEnum,
        rightButtonEnum: ActionBarRightButtonEnum
    ) {
        viewBinding = viewDataBinding
        title?.let { initActionBar(getString(it), leftButtonEnum, rightButtonEnum) }
    }

    protected fun initActionBar(title: String?, leftButtonEnum: ActionBarLeftButtonEnum, rightButtonEnum: ActionBarRightButtonEnum) {
        initActionBar(title, leftButtonEnum, rightButtonEnum, ActionBarRightSecondButtonEnum.NONE)
    }

    protected fun initActionBar(
        viewDataBinding: ViewCommonActionBarBinding,
        title: Int,
        leftButtonEnum: ActionBarLeftButtonEnum,
        rightButtonEnum: ActionBarRightButtonEnum,
        rightSecondButtonEnum: ActionBarRightSecondButtonEnum
    ) {
        viewBinding = viewDataBinding
        initActionBar(getString(title), leftButtonEnum, rightButtonEnum, rightSecondButtonEnum)
    }

    protected fun initActionBar(
        title: String?,
        leftButtonEnum: ActionBarLeftButtonEnum,
        rightButtonEnum: ActionBarRightButtonEnum,
        rightSecondButtonEnum: ActionBarRightSecondButtonEnum
    ) {
        title?.let { initActionBarName(it) }
        viewBinding?.apply {
            when (leftButtonEnum) {
                ActionBarLeftButtonEnum.BACK_BUTTON -> {
                    tvActionBarName.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                    ivActionBarLeftButton.setImageResource(R.drawable.btn_back)
                    ivActionBarLeftButton.visibility = View.VISIBLE
                    btnPrev.visibility = View.GONE
                    ivActionBarLeftButton.setOnClickListener {
                        onClickActionBarLeftButton()
                        activity?.onBackPressed()
                    }
                }

                ActionBarLeftButtonEnum.THEME -> {
                    ivActionBarLeftButton.setImageResource(R.drawable.ic_change)
                    ivActionBarLeftButton.setOnClickListener { onClickActionBarLeftButton() }
                    ivActionBarLeftButton.visibility = View.VISIBLE
                    btnPrev.visibility = View.GONE
                }

                ActionBarLeftButtonEnum.SETTING -> {
                    ivActionBarLeftButton.setImageResource(R.drawable.ic_setting)
                    ivActionBarLeftButton.setOnClickListener { onClickActionBarLeftButton() }
                    ivActionBarLeftButton.visibility = View.VISIBLE
                    btnPrev.visibility = View.GONE
                }

                ActionBarLeftButtonEnum.NONE -> {

                    ivActionBarLeftButton.visibility = View.INVISIBLE
                    btnPrev.visibility = View.GONE
                    ivActionBarLeftButton.setOnClickListener { }
                }

                ActionBarLeftButtonEnum.PREV -> {
                    ivActionBarLeftButton.visibility = View.INVISIBLE
                    btnPrev.visibility = View.VISIBLE
                    btnPrev.text = getString(R.string.prev)
                    btnPrev.setOnClickListener { onClickActionBarLeftButton() }
                }
            }

            when (rightButtonEnum) {
                ActionBarRightButtonEnum.COUPON -> {
                    ivActionBarRightButton.setImageResource(R.drawable.ic_coupon)
                    ivActionBarRightButton.setOnClickListener { onClickActionBarRightButton(ActionBarRightButtonEnum.COUPON) }
                    ivActionBarRightButton.visibility = View.VISIBLE
                }

                ActionBarRightButtonEnum.SETTINGS -> {
                    ivActionBarRightButton.setImageResource(R.drawable.ic_setting)
                    ivActionBarRightButton.setOnClickListener { onClickActionBarRightButton(ActionBarRightButtonEnum.SETTINGS) }
                    ivActionBarRightButton.visibility = View.VISIBLE
                }

                ActionBarRightButtonEnum.NEXT -> {
                    ivActionBarRightButton.visibility = View.GONE
                    tvActionBarName.visibility = View.GONE
                    tvActionBarTitle.visibility = View.VISIBLE
                    tvActionBarTitle.text = getString(R.string.theme_choice)
                    btnNext.visibility = View.VISIBLE
                    btnNext.text = getString(R.string.next)
                    btnNext.setOnClickListener { onClickActionBarRightButton(ActionBarRightButtonEnum.NEXT) }
                }

                else -> {
                }
            }
        }
    }

    fun setStatusBarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var window = requireActivity().window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = color
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
    }

    fun changeStatusBarColorWhite() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = requireActivity().window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.parseColor("#ffffff")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
    }

    fun changeStatusBarColorNavy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = requireActivity().window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.parseColor("#333743")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                var flags: Int = window.decorView.systemUiVisibility // get current flag
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // add LIGHT_STATUS_BAR to flag
                window.decorView.systemUiVisibility = flags
            }
//                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
//            Timber.d( "Fragment > onDestroy() > 리소스 해제 시작")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
//            Timber.d( "Fragment > onDestroyView() > 리소스 해제 시작")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}