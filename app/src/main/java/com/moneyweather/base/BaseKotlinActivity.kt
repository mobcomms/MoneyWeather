package com.moneyweather.base

import android.content.Intent
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.moneyweather.extensions.allowDisplayOnLockScreen
import com.moneyweather.util.CommonUtils
import com.moneyweather.view.LockScreenActivity


abstract class BaseKotlinActivity<T : ViewDataBinding, R : BaseKotlinViewModel> : BaseCommonActivity(), BaseKotlinViewModel.StartActivityListener {


    lateinit var viewDataBinding: T

    /**
     * setContentView로 호출할 Layout의 리소스 Id.
     * ex) R.layout.activity_sbs_main
     */
    abstract val layoutResourceId: Int

    /**
     * viewModel 로 쓰일 변수.
     */
    abstract val viewModel: R

    /**
     * 레이아웃을 띄운 직후 호출.
     * 뷰나 액티비티의 속성 등을 초기화.
     * ex) 리사이클러뷰, 툴바, 드로어뷰..
     */
    abstract fun initStartView()

    /**
     * 두번째로 호출.
     * 데이터 바인딩 및 rxjava 설정.
     * ex) rxjava observe, databinding observe..
     */
//    abstract fun initDataBinding()

    /**
     * 바인딩 이후에 할 일을 여기에 구현.
     * 그 외에 설정할 것이 있으면 이곳에서 설정.
     * 클릭 리스너도 이곳에서 설정.
     */
//    abstract fun initAfterBinding()

    private var isSetBackButtonValid = false

    var isLockScreen: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        isLockScreen = intent?.getBooleanExtra(LockScreenActivity.EXTRA_IS_LOCK_SCREEN, true) ?: true
        if (isLockScreen) {
            allowDisplayOnLockScreen()
        }

        viewDataBinding = DataBindingUtil.setContentView(this, layoutResourceId)
        viewDataBinding.lifecycleOwner = this

        CommonUtils.setActivitySystemBarPadding(viewDataBinding.root)

        viewModel.startActivityListener = this
        lifecycle.addObserver(viewModel)

        viewModel.activityFinish.observe(this) { if (it) finish() }
        viewModel.finishAffinity.observe(this) { if (it) finishAffinity() }

        initStartView()
    }

    override fun onStartActivity(intent: Intent) {
        startActivity(intent)
    }

    override fun onStartActivityForResult(intent: Intent, requestCode: Int) {
        startActivityForResult(intent, requestCode)
    }


    override fun onSoftKeyboardStatus(status: Boolean) {
        super.onSoftKeyboardStatus(status)
        viewModel.onSoftKeyboardStatus(status)
    }
}