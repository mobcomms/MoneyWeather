package com.moneyweather.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.moneyweather.R
import com.moneyweather.service.LockScreenService.Companion.serviceIntent
import com.moneyweather.util.CustomToast
import com.moneyweather.util.PrefRepository

class AppUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            if (intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
                context?.let {
                    startLockScreen(context)
                    CustomToast.showToast(context, R.string.app_update_message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startLockScreen(context: Context?) {
        PrefRepository.SettingInfo.useLockScreen = true

        try {
            if (serviceIntent == null) {
                serviceIntent = Intent(context, LockScreenService::class.java)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.startForegroundService(serviceIntent)
            } else {
                context?.startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}