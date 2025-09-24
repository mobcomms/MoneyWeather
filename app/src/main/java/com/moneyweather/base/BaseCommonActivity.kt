package com.moneyweather.base

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.annotation.NonNull
import com.google.gson.Gson
import com.moneyweather.R
import com.moneyweather.databinding.ViewCommonActionBarBinding
import com.moneyweather.listener.ActionBarButtonEventListener
import com.moneyweather.listener.SoftKeyboardStatusListener
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.model.enums.ActionBarRightButtonEnum
import com.moneyweather.model.enums.ActionBarRightSecondButtonEnum
import com.moneyweather.util.CustomToast
import com.moneyweather.util.PrefRepository
import com.moneyweather.view.MyCouponActivity
import timber.log.Timber


open class BaseCommonActivity : BaseActivity(), ActionBarButtonEventListener, View.OnClickListener, View.OnLayoutChangeListener,
    SoftKeyboardStatusListener {

    var searchKeywordHint: String? = null
    val TAG = this::class.java.simpleName

    lateinit var actionBarBinding: ViewCommonActionBarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColorWhite()
    }

    val MAX_BUNDLE_SIZE = 300
    override fun onSaveInstanceState(@NonNull outState: Bundle) {
        super.onSaveInstanceState(outState)
        val bundleSize = getBundleSize(outState)
        if (bundleSize > MAX_BUNDLE_SIZE * 1024) {
            outState.clear()
        }
    }

    private fun getBundleSize(bundle: Bundle): Long {
        val dataSize: Long
        val obtain = Parcel.obtain()
        dataSize = try {
            obtain.writeBundle(bundle)
            obtain.dataSize().toLong()
        } finally {
            obtain.recycle()
        }
        return dataSize
    }


    override fun onSearchClose() {
    }

    override fun onSearchStart(keyword: String) {
    }

    override fun onClickActionBarLeftButton() {
    }

    override fun onClickActionBarRightButton(rightSecondButtonEnum: ActionBarRightButtonEnum) {
        when (rightSecondButtonEnum) {
            ActionBarRightButtonEnum.COUPON -> {
                if (PrefRepository.UserInfo.isLogin) {
                    startActivity(MyCouponActivity::class.java)
                } else {
                    CustomToast.showToast(applicationContext, R.string.message_non_login_user)
                }
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

        if (actionBarBinding.tvActionBarName != null) {
            actionBarBinding.tvActionBarName.text = str
            actionBarBinding.tvActionBarName.setOnClickListener { onClickActionBarTitle() }
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

    protected fun initActionBar(binding: ViewCommonActionBarBinding, title: Int?, leftButtonEnum: ActionBarLeftButtonEnum) {
        actionBarBinding = binding
        title?.let { initActionBar(getString(it), leftButtonEnum, ActionBarRightButtonEnum.NONE) }
    }

    protected fun initActionBar(binding: ViewCommonActionBarBinding, title: String?, leftButtonEnum: ActionBarLeftButtonEnum) {
        actionBarBinding = binding
        title?.let { initActionBar(it, leftButtonEnum, ActionBarRightButtonEnum.NONE) }
    }

    protected fun initActionBar(
        binding: ViewCommonActionBarBinding,
        title: Int?,
        leftButtonEnum: ActionBarLeftButtonEnum,
        rightButtonEnum: ActionBarRightButtonEnum
    ) {
        actionBarBinding = binding
        title?.let { initActionBar(getString(it), leftButtonEnum, rightButtonEnum) }
    }

    protected fun initActionBar(title: String?, leftButtonEnum: ActionBarLeftButtonEnum, rightButtonEnum: ActionBarRightButtonEnum) {
        initActionBar(title, leftButtonEnum, rightButtonEnum, ActionBarRightSecondButtonEnum.NONE)
    }

    protected fun initActionBar(
        binding: ViewCommonActionBarBinding,
        title: Int,
        leftButtonEnum: ActionBarLeftButtonEnum,
        rightButtonEnum: ActionBarRightButtonEnum,
        rightSecondButtonEnum: ActionBarRightSecondButtonEnum
    ) {
        actionBarBinding = binding
        initActionBar(getString(title), leftButtonEnum, rightButtonEnum, rightSecondButtonEnum)
    }

    protected fun initActionBar(
        title: String?,
        leftButtonEnum: ActionBarLeftButtonEnum,
        rightButtonEnum: ActionBarRightButtonEnum,
        rightSecondButtonEnum: ActionBarRightSecondButtonEnum
    ) {


        title?.let { initActionBarName(it) }
        with(actionBarBinding) {
            when (leftButtonEnum) {
                ActionBarLeftButtonEnum.BACK_BUTTON -> {
                    ivActionBarLeftButton.setImageResource(R.drawable.btn_back)
                    ivActionBarLeftButton.visibility = View.VISIBLE
                    btnPrev.visibility = View.GONE
                    tvActionBarName.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                    ivActionBarLeftButton.setOnClickListener {
                        onClickActionBarLeftButton()
                        onBackPressed()
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
                    ivActionBarRightButton.setOnClickListener { onClickActionBarRightButton(rightButtonEnum) }
                    ivActionBarRightButton.visibility = View.VISIBLE
                    btnNext.visibility = View.GONE
                }

                ActionBarRightButtonEnum.NONE -> {
                    ivActionBarRightButton.visibility = View.INVISIBLE
                    btnNext.visibility = View.GONE
                    ivActionBarRightButton.setOnClickListener { }
                }

                ActionBarRightButtonEnum.NEXT -> {
                    ivActionBarRightButton.visibility = View.GONE
                    btnNext.visibility = View.VISIBLE
                    btnNext.text = getString(R.string.next)
                    btnNext.setOnClickListener { onClickActionBarRightButton(rightButtonEnum) }
                }

                else -> {
                }
            }
        }
    }

    fun setStatusBarColor(color: Int) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = color
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }, 200)
    }

    fun changeStatusBarColorWhite() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            runOnUiThread {
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = Color.parseColor("#ffffff")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    fun changeStatusBarColorNavy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            runOnUiThread {
                //해당 코드로 인해 fragment replace 시 비정상 동작 함
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = Color.parseColor("#333743")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    window.decorView.systemUiVisibility = 0
            }
        }
    }

    override fun onClick(v: View?) {
    }

    /**
     * 최상위 뷰의 사이즈 변경을 통해 키보드가 올라왔는지 여부를 체크 한다.
     */
    override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {

//        if (rootViewContainer == null)
//            return
//        Timber.d("oldBottom", oldBottom)
//        Timber.d("bottom", bottom)
//        rootViewContainer!!.removeOnLayoutChangeListener(this)
//        Timber.d("SDK_INT = " + Build.VERSION.SDK_INT)
//        Timber.d("rootViewHeight", rootViewHeight)
//        Timber.d("rootViewContainer!!.height", rootViewContainer!!.height)
//        window?.decorView?.height?.let { Timber.d("decorViewHeight", it) }
//
//        var status = bottom <= oldBottom
//
//        if (bottom == oldBottom && rootViewHeight > rootViewContainer!!.height)
//            status = false // 내려감 처리
//        onSoftKeyboardStatus(status)
//        Handler().postDelayed(Runnable { rootViewContainer?.addOnLayoutChangeListener(this) }, 200)
    }

    override fun onSoftKeyboardStatus(status: Boolean) {
//        if (!status) {//키보드 내려가 있는 상태
//            Timber.d("키보드 내려감 >>" + rootViewHeight)
////            (fragmentAdapter.getItem(viewPagerMain.currentItem) as BaseFragment).onSoftKeyboard(false)
//        } else {
//            Timber.d("키보드 올라감")
////            (fragmentAdapter.getItem(viewPagerMain.currentItem) as BaseFragment).onSoftKeyboard(true)
//        }
//        LogPrint.line()
    }


    override fun onResume() {
        super.onResume()
//        Timber.d( "TESTSSS, onResume")
        onBundle(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
//        Timber.d( "TESTSSS, onNewIntent")
        onBundle(intent)
    }

    private fun onBundle(intent: Intent?) {

    }

}