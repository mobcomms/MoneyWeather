package com.moneyweather.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.moneyweather.service.LockScreenService.Companion.serviceIntent
import com.moneyweather.util.PermissionUtils
import com.moneyweather.util.PrefRepository
import com.moneyweather.view.SplashActivity

class RebootAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val isGrantedPermission = PermissionUtils.isGrantedPermission(
            context, SplashActivity.REQUEST_LOCATION_PERMISSIONS)

        val isGrantedServicePermission = PermissionUtils.isGrantedPermission(
            context, SplashActivity.REQUEST_SERVICE_PERMISSION)

        if (!isGrantedPermission || !isGrantedServicePermission) return

        PrefRepository.SettingInfo.useLockScreen = true

        if (serviceIntent == null) {
            serviceIntent = Intent(context, LockScreenService::class.java)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
