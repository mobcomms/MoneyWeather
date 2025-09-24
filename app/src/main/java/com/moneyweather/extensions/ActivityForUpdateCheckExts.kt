package com.moneyweather.extensions

import android.app.Activity
import android.app.NotificationManager
import android.app.Service.NOTIFICATION_SERVICE
import android.content.Intent
import android.net.Uri
import com.moneyweather.R
import com.moneyweather.data.remote.response.AppVersionResponse
import com.moneyweather.model.enums.DialogType
import com.moneyweather.service.LockScreenService
import com.moneyweather.service.LockScreenService.Companion.CHANNEL_ID
import com.moneyweather.ui.dialog.HCCommonDialog
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.PrefRepository
import com.moneyweather.view.LockScreenActivity
import com.moneyweather.view.MainActivity
import kotlin.math.max

fun Activity.checkUpdate(
    data: AppVersionResponse.Data,
    onCancel: () -> Unit = {}
): Boolean {
    return try {
        val currentVersion = CommonUtils.getAppVersion() ?: return false
        val checkLatestVersion = compareVersion(currentVersion, data.latestVersion ?: return false)
        val checkMinSupportedVersion = compareVersion(currentVersion, data.minSupportedVersion ?: return false)

        // 최신 버전인 경우
        if (!checkLatestVersion) {
            return false
        }

        // 강제 업데이트인 경우
        if (checkMinSupportedVersion || "FORCE" == data.updateType) {
            popupImmediateUpdate()
            return true
        } else {
            // 권장 업데이트인 경우
            if ("RECOMMENDED" == data.updateType) {
                val shouldShowContent = CommonUtils.shouldShowContent(
                    PrefRepository.SettingInfo.checkUpdatePopup
                )

                if (shouldShowContent) {
                    popupFlexibleUpdate(onCancel)
                    return true
                }
            }
            return false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

private fun compareVersion(appVersion: String, compareVersion: String): Boolean {
    var isNeedUpdate = false
    val arrX = appVersion.split("[.]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val arrY = compareVersion.split("[.]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val length = max(arrX.size.toDouble(), arrY.size.toDouble()).toInt()

    for (i in 0 until length) {
        var x = try {
            arrX[i].toInt()
        } catch (e: ArrayIndexOutOfBoundsException) {
            0
        }

        var y = try {
            arrY[i].toInt()
        } catch (e: ArrayIndexOutOfBoundsException) {
            0
        }

        if (x > y) {
            isNeedUpdate = false
            break
        } else if (x < y) {
            isNeedUpdate = true
            break
        } else {
            isNeedUpdate = false
        }
    }

    return isNeedUpdate
}

fun Activity.popupImmediateUpdate() {
    val dialog: HCCommonDialog = HCCommonDialog(this)
        .setDialogType(DialogType.ALERT)
        .setLayout(
            R.layout.popup_update_check,
            resources.getString(R.string.popup_update_check_content1)
        )
        .setPositiveButtonText(R.string.popup_update_check_go_store)
        .setNegativeButtonText(R.string.popup_update_check_app_finish)
        .setOnDismissListener(object : HCCommonDialog.OnDismissListener {
            override fun onDismiss(menuId: Int) {
                when (menuId) {
                    DialogType.BUTTON_POSITIVE.ordinal -> {
                        if (this@popupImmediateUpdate is LockScreenActivity) {
                            startActivity()
                        } else {
                            goToGooglePlayStore()
                        }
                    }

                    DialogType.BUTTON_NEGATIVE.ordinal -> {
                        finishActivity()
                    }
                }
            }
        })
    dialog.setCancelable(false)
    dialog.show()
}

fun Activity.popupFlexibleUpdate(onCancel: () -> Unit) {
    PrefRepository.SettingInfo.checkUpdatePopup = CommonUtils.getCurrentDate()

    val dialog: HCCommonDialog = HCCommonDialog(this)
        .setDialogType(DialogType.ALERT)
        .setLayout(
            R.layout.popup_update_check,
            resources.getString(R.string.popup_update_check_content2)
        )
        .setPositiveButtonText(R.string.popup_update_check_go_store)
        .setNegativeButtonText(R.string.popup_update_check_next_time)
        .setOnDismissListener(object : HCCommonDialog.OnDismissListener {
            override fun onDismiss(menuId: Int) {
                when (menuId) {
                    DialogType.BUTTON_POSITIVE.ordinal -> {
                        if (this@popupFlexibleUpdate is LockScreenActivity) {
                            startActivity()
                        } else {
                            goToGooglePlayStore()
                        }
                    }

                    DialogType.BUTTON_NEGATIVE.ordinal -> {
                        onCancel()
                    }
                }
            }
        })
    dialog.setCancelable(false)
    dialog.show()
}

fun Activity.finishActivity() {
    if (this is LockScreenActivity) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(CHANNEL_ID)

        LockScreenService.serviceIntent?.let { stopService(it) }
    }
    finish()
}

fun Activity.startActivity() {
    val intent = Intent(this, MainActivity::class.java)
    intent.putExtra("is_google_store", true)
    startActivity(intent)
    finishActivity()
}

fun Activity.goToGooglePlayStore() {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.addCategory(Intent.CATEGORY_DEFAULT)
    intent.setData(Uri.parse("market://details?id=${packageName}"))
    startActivity(intent)
    finishActivity()
}