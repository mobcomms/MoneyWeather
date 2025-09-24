package com.moneyweather.view

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.databinding.ActivitySettingBinding
import com.moneyweather.fcm.FCMService.ServiceType
import com.moneyweather.listener.AppFinishListener
import com.moneyweather.model.enums.ActivityEnum
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.PermissionUtils
import com.moneyweather.view.LockScreenActivity.Companion.EXTRA_IS_LOCK_SCREEN
import com.moneyweather.view.fragment.FaqFragment
import com.moneyweather.view.fragment.NoticeDetailFragment
import com.moneyweather.view.fragment.SettingFragment
import com.moneyweather.view.fragment.ThemeSettingFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable

@AndroidEntryPoint
class SettingActivity : BaseKotlinActivity<ActivitySettingBinding, BaseKotlinViewModel>(),
    View.OnClickListener,
    AppFinishListener {

    override val layoutResourceId: Int get() = R.layout.activity_setting
    override val viewModel: BaseKotlinViewModel by viewModels()

    private var moveActivity: ActivityEnum? = null
    private var serviceType: ServiceType? = null
    private var title: String? = ""
    private var content: String? = ""
    private var createdAt: String? = ""

    override fun initStartView() {

        intent?.let {
            moveActivity = try {
                it.intentSerializable("move_activity", ActivityEnum::class.java)
            } catch (e: Exception) {
                ActivityEnum.SETTING
            }

            serviceType = try {
                it.intentSerializable("service_type", ServiceType::class.java)
            } catch (e: Exception) {
                null
            }

            title = try {
                it.getStringExtra("title")
            } catch (e: Exception) {
                ""
            }
            content = try {
                it.getStringExtra("content")
            } catch (e: Exception) {
                ""
            }
            createdAt = try {
                it.getStringExtra("createdAt")
            } catch (e: Exception) {
                ""
            }

            isLockScreen = try {
                it.getBooleanExtra(EXTRA_IS_LOCK_SCREEN, true)
            } catch (e: Exception) {
                false
            }
        }

        replaceFragment(R.id.fragmentContainer, SettingFragment::class.java)

        viewDataBinding.apply {
            moveActivity?.let {
                when (moveActivity) {
                    ActivityEnum.THEME_SETTING -> {
                        intent.putExtra("type", "lockscreen")
                        replaceFragment(R.id.fragmentContainer, ThemeSettingFragment::class.java, intent)
                    }

                    ActivityEnum.NOTICE -> {
                        intent.putExtra("move_tab", "notice")
                        replaceFragment(R.id.fragmentContainer, SettingFragment::class.java, intent)
                    }

                    ActivityEnum.NOTICE_DETAIL -> {
                        var bundle = Bundle()
                        bundle.putString("title", title)
                        bundle.putString("content", content)
                        bundle.putString("createdAt", createdAt)

                        intent.putExtras(bundle)
                        startFragment(R.id.fragmentContainer, NoticeDetailFragment::class.java, intent)
                    }

                    ActivityEnum.SETTING -> {
                        if (serviceType != null) {
                            when (ServiceType.fromCode(serviceType!!.code)) {
                                ServiceType.FAQ -> replaceFragment(R.id.fragmentContainer, FaqFragment::class.java)
                                else -> {}
                            }
                        }
                    }

                    else -> {
                        replaceFragment(R.id.fragmentContainer, SettingFragment::class.java)
                    }
                }
            }
        }

        val startPoint = if (isLockScreen) {
            "lockScreen"
        } else {
            "inApp"
        }

        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "설정")
            putString(FirebaseAnalyticsManager.START_POINT, startPoint)
        })
    }

    override fun onFinish(dialog: Dialog) {

    }

    fun <T : Serializable> Intent.intentSerializable(key: String, clazz: Class<T>): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.getSerializableExtra(key, clazz)
        } else {
            this.getSerializableExtra(key) as T?
        }
    }

    fun checkPermission() {
        requestNeededPermission {

        }
    }

    private fun loadLocation() {
        CommonUtils.getLocation()
    }

    private fun requestNeededPermission(callback: () -> Unit) {
        val granted = PermissionUtils.isGrantedPermission(this, REQUEST_PERMISSIONS)
        if (!granted) {
            requestNeededPermission.launch(REQUEST_PERMISSIONS)
        } else {
            callback()
        }
    }

    private val requestNeededPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            var isAllGrant = true
            for (isGrant in it) {
                if (!isGrant.value)
                    isAllGrant = false
            }
            loadLocation()

        }

    private val REQUEST_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
}


