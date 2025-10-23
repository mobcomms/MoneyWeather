package com.moneyweather.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import androidx.multidex.MultiDex
import com.buzzvil.buzzbenefit.BuzzBenefitConfig
import com.buzzvil.sdk.BuzzvilSdk
import com.enliple.banner.MobSDK
import com.enliple.banner.common.MobConstant
import com.enliple.datamanagersdk.ENDataManager
import com.gomfactory.adpie.sdk.AdPieSDK
import com.google.firebase.crashlytics.CustomKeysAndValues
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.mobwith.MobwithSDK
import com.moneyweather.BuildConfig
import com.moneyweather.util.AppLifeCycleTracker
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.NetworkUtils
import com.moneyweather.util.PrefRepository
import com.navercorp.nid.NaverIdLoginSDK
import com.uber.rxdogtag.RxDogTag
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


@HiltAndroidApp
class BaseApplication : Application() {
    private var activityLifecycleCallbacks: AppLifeCycleTracker? = null

    override fun onCreate() {
        instance = this

        Timber.d("onCreate()")
        println("!!!!!!!!!!!!!!!!!!!!!!! keyHash :  ${Utility.getKeyHash(this)}")

        super.onCreate()

        initMaterialCalendar()
        initActivityLifecycleCallbacks()
        initLog()
        initRxDogTag()

        // AD SDK
        CoroutineScope(Dispatchers.Default).launch {
            initAnicGameZoneSDK()
            initAdPieSDK()
            initMobwithSDK()
            initENDataManager()
            initBuzzvilSDK()
        }

        // Social SDK
        initKakaoSDK()
        initNaverSDK()

        // Google
        initFirebase()
    }

    private fun initMaterialCalendar() {
        AndroidThreeTen.init(this)
    }

    private fun initActivityLifecycleCallbacks() {
        if (activityLifecycleCallbacks == null) {
            activityLifecycleCallbacks = AppLifeCycleTracker({ startActivity, activityStack ->
                Timber.tag(AppLifeCycleTracker.TAG).d("startActivity=${startActivity::class.java.simpleName}, activityStack=${activityStack}")
            }, { destroyActivity, activityStack ->
                Timber.tag(AppLifeCycleTracker.TAG).d("destroyActivity=${destroyActivity::class.java.simpleName}, activityStack=${activityStack}")
            })
        }

        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    private fun initLog() {
        if (isDebuggable()) {
            Timber.plant(Timber.DebugTree())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                StrictMode.setVmPolicy(
                    VmPolicy.Builder()
                        .detectUnsafeIntentLaunch()
                        .build()
                )
            }
        }
    }

    private fun initAnicGameZoneSDK() {
        MobSDK.init(appContext(), this, ANIC_MEDIA_CODE)
        MobSDK.setBannerViewMode(MobConstant.AD_VIEW_MODE_ACTIVITY)
        MobSDK.setDebug(false)
    }

    private fun initAdPieSDK() {
        AdPieSDK.getInstance().initialize(this, BuildConfig.ADPIE_MEDIA_ID)
    }

    private fun initENDataManager() {
        ENDataManager.init(this, BuildConfig.TUNE_APP_KEY)
    }

    private fun initMobwithSDK() {
        MobwithSDK.getInstance().apply {
            setUnityGameId(this@BaseApplication, BuildConfig.UNITY_ADS_GAME_ID)
            setLevelPlayAppKey(this@BaseApplication, BuildConfig.LEVEL_PLAY_APP_KEY)
            setDTExChangeAppKey(BuildConfig.DT_EXCHANGE_APP_KEY)
        }
    }

    private fun initKakaoSDK() {
        /* V2업데이트 */
        KakaoSdk.init(this, BuildConfig.KAKAO_APP_KEY)

    }

    private fun initNaverSDK() {
        NaverIdLoginSDK.initialize(
            appContext(),
            BuildConfig.NAVER_CLIENT_ID,
            BuildConfig.NAVER_CLIENT_SECRET,
            BuildConfig.NAVER_CLIENT_NAME
        )
    }

    private fun initFirebase() {
        FirebaseAnalyticsManager.init()
        setFirebaseCrashlytics()
        setFirebaseRemoteConfig()
    }

    private fun initRxDogTag() {
        RxDogTag.install()
    }

    private fun initBuzzvilSDK() {
        val buzzBenefitConfig = BuzzBenefitConfig
            .Builder(if (BuildConfig.DEBUG) BuildConfig.BUZZVIL_APP_ID_DEV else BuildConfig.BUZZVIL_APP_ID)
            .build()

        BuzzvilSdk.initialize(
            application = this@BaseApplication,
            buzzBenefitConfig = buzzBenefitConfig
        )
    }

    override fun onTerminate() {
        Timber.d("onTerminate()")
        super.onTerminate()
        onDestroy()
    }

    fun onDestroy() {
        Timber.d("onDestroy()")
        try {
            unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
            instance = null
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)//Added by JBK 210402, 덱스 가드 제거 후 멀티덱스 재 활성화
    }

    private fun setFirebaseCrashlytics() {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        val userId = PrefRepository.UserInfo.userId
        val keysAndValues = CustomKeysAndValues.Builder()
            .putString("Device_Model", Build.MODEL)
            .putString("OS_Version", Build.VERSION.RELEASE)
            .putString("Network_Type", NetworkUtils.getNetworkConnectionType(this))
            .putString("Battery_Level", CommonUtils.getBatteryLevel(this))
            .putBoolean("isRooting", CommonUtils.checkSuperUser())
            .putBoolean("isEmulator", CommonUtils.isEmulator())
            .build()

        FirebaseCrashlytics.getInstance().setUserId(userId)
        FirebaseCrashlytics.getInstance().setCustomKeys(keysAndValues)
    }

    private fun setFirebaseRemoteConfig() {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }

        Firebase.remoteConfig.setConfigSettingsAsync(configSettings)
    }

    companion object {
        private const val ANIC_MEDIA_CODE = "MONEYWEATHER"

        @SuppressLint("StaticFieldLeak")
        var instance: BaseApplication? = null

        fun appContext(): Context {
            return instance?.applicationContext!!
        }

        @JvmStatic
        fun isDebuggable(): Boolean {
            var debuggable = false
            try {
                val pm = instance?.packageManager
                val appinfo = pm?.getApplicationInfo(instance!!.packageName, 0)
                debuggable = 0 != appinfo!!.flags and ApplicationInfo.FLAG_DEBUGGABLE
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return debuggable
        }

        fun unlockScreen() {

        }

        fun lockScreen() {

        }
    }
}