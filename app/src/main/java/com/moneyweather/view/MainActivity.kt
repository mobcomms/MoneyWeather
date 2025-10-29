package com.moneyweather.view

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.buzzvil.sdk.BuzzvilSdk
import com.buzzvil.sdk.BuzzvilSdkUser
import com.enliple.banner.MobSDK
import com.enliple.datamanagersdk.ENDataManager
import com.enliple.datamanagersdk.events.models.ENPageView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.analytics.FirebaseAnalytics
import com.igaworks.adpopcorn.Adpopcorn
import com.moneyweather.BuildConfig
import com.moneyweather.R
import com.moneyweather.adapter.MainBottomAdapter
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.databinding.ActivityMainBinding
import com.moneyweather.event.main.MainUiEvent
import com.moneyweather.extensions.createTnkOfferwall
import com.moneyweather.extensions.goToGooglePlayStore
import com.moneyweather.fcm.FCMService.ServiceType
import com.moneyweather.listener.AppFinishListener
import com.moneyweather.model.CategoryItem
import com.moneyweather.model.enums.ActivityEnum
import com.moneyweather.model.enums.DialogType
import com.moneyweather.model.enums.LandingPageEnum
import com.moneyweather.service.LockScreenService
import com.moneyweather.service.LockScreenService.Companion.serviceIntent
import com.moneyweather.ui.dialog.AppEndDialog
import com.moneyweather.ui.dialog.HCCommonDialog
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.CustomToast
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.NetworkUtils
import com.moneyweather.util.PermissionUtils
import com.moneyweather.util.PrefRepository
import com.moneyweather.util.analytics.GaButtonClickEvent
import com.moneyweather.util.fagmentFactory.MainFragmentFactory
import com.moneyweather.view.LockScreenWebViewActivity.Companion.KEY_LOAD_URL
import com.moneyweather.view.LockScreenWebViewActivity.Companion.KEY_VIEW_TYPE
import com.moneyweather.viewmodel.MainViewModel
import com.pincrux.offerwall.PincruxOfferwall
import com.tnkfactory.ad.TnkOfferwall
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MainActivity : BaseKotlinActivity<ActivityMainBinding, MainViewModel>(), View.OnClickListener,
    AppFinishListener {

    override val layoutResourceId: Int get() = R.layout.activity_main
    override val viewModel: MainViewModel by viewModels()

    val bottomAdapter: MainBottomAdapter by lazy {
        MainBottomAdapter(
            this,
            supportFragmentManager,
            lifecycle
        )
    }
    private var moveActivity: ActivityEnum? = null
    private var serviceType: ServiceType? = null
    private var link: String? = ""
    private var isWeatherUpdate: Boolean = false
    private var path: String? = null
    private var isOfferwall: Boolean = false
    private var isGooglePlayStore: Boolean = false

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "홈")
        })

        instance = this
        viewDataBinding.vm = viewModel
        //viewModel.connectUserInfo()
        initBottomTabSetting()
        checkPermission()
        checkLocation()
        popupAdPushAgree()
        viewModel.connectConfigInit()
        viewModel.connectUserInfo()

        intent?.let { moveLandingPage(it) }

        viewModel.user?.observe(this) {
            FirebaseAnalyticsManager.setUserId(it.userId)
            getTnkOfferwall(this, it.userId)
            Adpopcorn.setUserId(this@MainActivity, it.userId)
            getPincruxOfferwall().init(this, BuildConfig.PINCRUX_PUB_KEY, it.userId)
            BuzzvilSdk.login(BuzzvilSdkUser(userId = it.userId))
        }

        viewModel.resultNoticeDetail?.observe(this, Observer {
            try {
                it?.let {
                    startActivity(
                        Intent(this@MainActivity, SettingActivity::class.java)
                            .putExtra("move_activity", ActivityEnum.NOTICE_DETAIL)
                            .putExtra("title", it.title)
                            .putExtra("content", it.content)
                            .putExtra("createdAt", it.createdAt)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        onBackPressedDispatcher.addCallback(this@MainActivity, onBackPressedCallback)

        viewModel.dispatchEvent(MainUiEvent.RefreshFcmToken)
        viewModel.dispatchEvent(MainUiEvent.FetchPomissionZoneUrl)
    }

    private fun checkPermission() {
        requestNeededPermission { startLockScreen() }
    }

    fun requestNeededPermission(callback: () -> Unit) {
        val isGrantedPermission = PermissionUtils.isGrantedPermission(
            this@MainActivity, SplashActivity.REQUEST_LOCATION_PERMISSIONS
        )

        val isGrantedServicePermission = PermissionUtils.isGrantedPermission(
            this@MainActivity, SplashActivity.REQUEST_SERVICE_PERMISSION
        )

        val canDrawOverlays = Settings.canDrawOverlays(this@MainActivity)

        if (isGrantedPermission && isGrantedServicePermission && canDrawOverlays) {
            callback()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.fragmentFactory = MainFragmentFactory()
    }

    override fun onResume() {
        super.onResume()
        isWeatherUpdate = true

        if (NetworkUtils.checkNetworkState(this@MainActivity)) {
            updateWeather()
            viewModel.connectUserPoint()
        } else {
            CustomToast.showToast(this@MainActivity, getString(R.string.network_connection_check))
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        isWeatherUpdate = false
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseBuzzvil()
    }

    private fun releaseBuzzvil() {
        if (BuzzvilSdk.isLoggedIn) {
            BuzzvilSdk.logout()
        }
    }

    override fun onFinish(dialog: Dialog) {
        TODO("Not yet implemented")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent?.let { moveLandingPage(it) }
    }

    fun startLockScreen() {
        if (!PrefRepository.SettingInfo.useLockScreen) {
            stopLockScreen()
            return
        }

        if (serviceIntent == null) {
            serviceIntent = Intent(this, LockScreenService::class.java)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    fun stopLockScreen() {
        if (serviceIntent != null) {
            stopService(serviceIntent)
        }
    }

    private fun checkLocation() {
        if (!CommonUtils.isLocationEnabled(this@MainActivity)) {
            popupLocationSetting()
        }
    }

    private fun updateWeather() {
        if (CommonUtils.isLocationEnabled(this@MainActivity)) {
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    while (isWeatherUpdate) {
                        loadLocation()
                        delay(1800000) // 30분마다 갱신
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadLocation() {
        CommonUtils.getLocation(
            onSuccessCallbackListener = object : CommonUtils.Companion.OnCallbackLocationListener {
                override fun onCallbackLocation(lat: String, lon: String) {
                    viewModel.getCurrentWeather(lat, lon)
                }
            },
            onFailureCallbackListener = object : CommonUtils.Companion.OnCallbackLocationListener {
                override fun onCallbackLocation(lat: String, lon: String) {
                    viewModel.getCurrentWeather(lat, lon)
                }
            })
    }

    private fun popupLocationSetting() {
        val dialog: HCCommonDialog = HCCommonDialog(this@MainActivity)
            .setDialogType(DialogType.ALERT)
            .setLayout(R.layout.popup_location_check, resources.getString(R.string.popup_location_check_content))
            .setPositiveButtonText(R.string.setting)
            .setNegativeButtonText(R.string.cancel)
            .setOnDismissListener(object : HCCommonDialog.OnDismissListener {
                override fun onDismiss(menuId: Int) {
                    when (menuId) {
                        DialogType.BUTTON_POSITIVE.ordinal -> requestLocationSetting()
                        DialogType.BUTTON_NEGATIVE.ordinal -> {
                            CustomToast.showToast(this@MainActivity, R.string.toast_location_check_content)
                            loadLocation()
                        }
                    }
                }
            })
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun requestLocationSetting() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        locationResultLauncher.launch(intent)
    }

    private var locationResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (!CommonUtils.isLocationEnabled(this@MainActivity)) {
            CustomToast.showToast(this@MainActivity, R.string.toast_location_check_content)
        }
        loadLocation()
    }

    private fun initBottomTabSetting() {
        val bottomCategory = makeBottomCategory()

        viewDataBinding.apply {
            vpPage.apply {
                adapter = bottomAdapter
                isUserInputEnabled = false
                offscreenPageLimit = 4//bottomAdapter.itemCount
            }

            TabLayoutMediator(tabBottom, vpPage) { tab, position ->
                tab.text = bottomCategory[position].name
                tab.icon = getDrawable(bottomCategory[position].iconRes)
                tab.view.setOnClickListener {
                    // Ga Log Event
                    GaButtonClickEvent.logMainTabButtonClickEvent(mainTabPos, position)
                    // Tune720 Log Event
                    onSelectTabWithTune720(position)
                }
            }.attach()

            tabBottom.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.position?.let { position ->
                        mainTabPos = position
                        vpPage.setCurrentItem(mainTabPos, false)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })
            tabBottom.selectTab(tabBottom.getTabAt(mainTabPos))
            vpPage.setCurrentItem(mainTabPos, false)
        }
    }

    private fun onSelectTabWithTune720(position: Int) {
        try {
            var pvName = ""
            when (position) {
                0 -> pvName = "view.fragment.MainHomeFragment"
                1 -> pvName = "view.fragment.ChargeFragment"
                2 -> pvName = "view.fragment.StoreFragment"
                3 -> pvName = "view.fragment.MyFragment"
            }

            if (ENDataManager.isInitialized() && pvName.isNotEmpty()) {
                val pageView = ENPageView(pvName, "")
                ENDataManager.getInstance().addEvent(pageView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun makeBottomCategory(): ArrayList<CategoryItem> {
        return arrayListOf(
            CategoryItem(
                TAB_HOME,
                getString(R.string.bottom_nav_home),
                R.drawable.selector_home_tab
            ),
//            CategoryItem(
//                TAB_CHARGE,
//                getString(R.string.bottom_nav_charge),
//                R.drawable.selector_charge_tab
//            ),
            CategoryItem(
                TAB_STORE,
                getString(R.string.bottom_nav_store),
                R.drawable.selector_store_tab
            ),
            CategoryItem(TAB_MY, getString(R.string.bottom_nav_my), R.drawable.selector_my_tab)
        )

    }

    fun moveTab(num: Int) {
        mainTabPos = num
        viewDataBinding.apply {
            tabBottom.selectTab(tabBottom.getTabAt(mainTabPos))
            vpPage.setCurrentItem(mainTabPos, false)
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (TAB_HOME == mainTabPos) {
//                finish()
                // 앱 종료 팝업 띄우기
                var dialog = AppEndDialog(finishButtonUnit = {finish()})
                dialog.show(supportFragmentManager, "appEndDialog")

            } else {
                moveTab(TAB_HOME)
            }
        }
    }

    companion object {
        var instance: MainActivity? = null
        const val TAB_HOME: Int = 0

        //        const val TAB_CHARGE: Int = 2
        const val TAB_STORE: Int = 1
        const val TAB_MY: Int = 2
        var mainTabPos = TAB_HOME

        private var tnkOfferwall: TnkOfferwall? = null
        private var pincruxOfferwall: PincruxOfferwall? = null

        fun getTnkOfferwall(context: Context, userId: String): TnkOfferwall {
            if (tnkOfferwall == null) {
                tnkOfferwall = createTnkOfferwall(context, userId)
            }
            return tnkOfferwall as TnkOfferwall
        }

        fun getPincruxOfferwall(): PincruxOfferwall {
            if (pincruxOfferwall == null) {
                pincruxOfferwall = PincruxOfferwall()
            }
            return pincruxOfferwall as PincruxOfferwall
        }
    }

    fun <T : Serializable> Intent.intentSerializable(key: String, clazz: Class<T>): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.getSerializableExtra(key, clazz)
        } else {
            this.getSerializableExtra(key) as T?
        }
    }

    /**
     * @param intent
     */
    private fun moveLandingPage(intent: Intent) {
        try {
            intent.apply {
                moveActivity = intentSerializable("move_activity", ActivityEnum::class.java)
                serviceType = intentSerializable("service_type", ServiceType::class.java)
                link = getStringExtra("link")
                path = getStringExtra("path")
                isOfferwall = getBooleanExtra("is_offerwall", false)
                isGooglePlayStore = getBooleanExtra("is_google_store", false)
            }

            if (isGooglePlayStore) {
                goToGooglePlayStore()
            }

            when (moveActivity) {
                ActivityEnum.NONE -> moveTab(TAB_HOME)

                ActivityEnum.SETTING -> {
                    val settingIntent = Intent(this, SettingActivity::class.java)
                    if (moveActivity != null) {
                        settingIntent.putExtra("move_activity", moveActivity)
                    }
                    if (serviceType != null) {
                        settingIntent.putExtra("service_type", serviceType)
                    }
                    startActivity(settingIntent)
                }

                ActivityEnum.GAMEZONE -> {
                    startActivity(Intent(this, MissionZoneActivity::class.java))
                }

                ActivityEnum.CHARGE -> {
//                    moveTab(1)

                    if (isOfferwall) {
                        PrefRepository.LockQuickInfo.lockLandingEventInfo?.let { data ->
                            if (data.lockLandingEventId > 0) {
                                viewModel.dispatchEvent(
                                    MainUiEvent.OpenOfferwall(this, data.landingEventId)
                                )
                            }
                        }
                    }
                }

                ActivityEnum.CHARGE_POMISSION_ZONE -> {
                    if (BuildConfig.DEBUG) {
                        CustomToast.showToast(this, R.string.message_on_debug_mode)
                        return
                    }

                    val pomissionZoneUrl = PrefRepository.LockQuickInfo.pomissionZoneUrl
                    if (pomissionZoneUrl.isEmpty()) return

//                    // 충전소 페이지 이동
//                    moveTab(1)

                    // pomission zone을 웹뷰로 노출
                    val pomissionZoneIntent = Intent(this, AppWebViewActivity::class.java).apply {
                        putExtra(KEY_LOAD_URL, pomissionZoneUrl)
                        putExtra(KEY_VIEW_TYPE, LockScreenWebViewActivity.ViewType.POMISSION_ZONE.name)
                        putExtra(LockScreenActivity.EXTRA_IS_LOCK_SCREEN, isLockScreen)
                    }
                    startActivity(pomissionZoneIntent)
                }

                ActivityEnum.NOTICE -> {
                    val settingIntent = Intent(this, SettingActivity::class.java)
                    settingIntent.putExtra("move_activity", ActivityEnum.NOTICE)
                    settingIntent.putExtra("link", link)
                    startActivity(settingIntent)
                }

                ActivityEnum.MYPOINT -> startActivity(MyPointActivity::class.java)

                ActivityEnum.NOTICE_DETAIL -> {
                    try {
                        link?.let { viewModel.connectNoticeDetail(link!!.toInt()) }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                ActivityEnum.SHOPLUS -> viewModel.startShoplus(this@MainActivity)

                ActivityEnum.EXTERNAL -> {
                    val intent = Intent(this, AppWebViewActivity::class.java)
                    intent.putExtra(KEY_LOAD_URL, link)
                    startActivity(intent)
                }

                else -> moveTab(TAB_HOME)
            }

            when (path) {
                LandingPageEnum.HOME.path -> moveTab(TAB_HOME)
//                LandingPageEnum.CHARGE.path -> moveTab(1)
                LandingPageEnum.STORE.path -> moveTab(TAB_STORE)
                LandingPageEnum.MY.path -> moveTab(TAB_MY)
                LandingPageEnum.MYPOINT.path -> startActivity(MyPointActivity::class.java)
                LandingPageEnum.SETTING.path -> startActivity(SettingActivity::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun popupAdPushAgree() {
        if (PrefRepository.UserInfo.isFirst) {
            HCCommonDialog(this).apply {
                setDialogType(DialogType.ALERT)
                setLayout(R.layout.popup_ad_push_agree)
                setPositiveButtonText(R.string.popup_confirm)
                setNegativeButtonText(R.string.popup_reject)
                setOnDismissListener(object : HCCommonDialog.OnDismissListener {
                    override fun onDismiss(menuId: Int) {
                        when (menuId) {
                            DialogType.BUTTON_POSITIVE.ordinal -> {
                                viewModel.dispatchEvent(
                                    MainUiEvent.UpdatePushAgree(
                                        servicePushAgreed = true,
                                        marketingPushAgreed = true,
                                        nightPushAllowed = true
                                    )
                                )

                                val msg = SimpleDateFormat(
                                    getString(R.string.toast_ad_push_agree_confirm),
                                    Locale.KOREA
                                ).format(Date())

                                CustomToast.showToast(this@MainActivity, msg)
                            }

                            DialogType.BUTTON_NEGATIVE.ordinal -> {
                                viewModel.dispatchEvent(
                                    MainUiEvent.UpdatePushAgree(
                                        servicePushAgreed = true,
                                        marketingPushAgreed = false,
                                        nightPushAllowed = true
                                    )
                                )

                                val msg = SimpleDateFormat(
                                    getString(R.string.toast_ad_push_agree_reject),
                                    Locale.KOREA
                                ).format(Date())

                                CustomToast.showToast(this@MainActivity, msg)
                            }
                        }
                    }
                })
                show()
                setCancelable(false)
            }

            PrefRepository.UserInfo.isFirst = false
        } else {
            viewModel.dispatchEvent(MainUiEvent.FetchPushAgree)
        }
    }
}