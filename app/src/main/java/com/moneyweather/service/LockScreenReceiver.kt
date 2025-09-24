package com.moneyweather.service

import android.Manifest
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.moneyweather.service.LockScreenService.Companion.PHONE_STATE_BROADCAST
import com.moneyweather.service.LockScreenService.Companion.PHONE_STATE_BROADCAST_OFF_HOOK
import com.moneyweather.util.PermissionUtils
import com.moneyweather.util.PrefRepository
import com.moneyweather.view.LockScreenActivity
import com.moneyweather.view.SplashActivity

class LockScreenReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val isGrantedPermission = PermissionUtils.isGrantedPermission(
            context, SplashActivity.REQUEST_LOCATION_PERMISSIONS
        )

        val isGrantedServicePermission = PermissionUtils.isGrantedPermission(
            context, SplashActivity.REQUEST_SERVICE_PERMISSION
        )

        if (!PrefRepository.SettingInfo.useLockScreen || !isGrantedPermission || !isGrantedServicePermission) return

        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                calledWhenOff = true
            }

            Intent.ACTION_SCREEN_ON -> {
                calledWhenOff = false

                startLockScreen(context)
            }

            PHONE_STATE_BROADCAST -> {
                if (!calledWhenOff) {
                    // 잠금 화면일 때 전화가 온 경우 돈씨 락스크린 화면 닫기
                    finishLockScreenActivity()
                }
            }

            PHONE_STATE_BROADCAST_OFF_HOOK -> {
                if (!calledWhenOff) {
                    // 잠금 화면에서 통화중인 경우 돈씨 락스크린 화면이 뜨지 않게 처리
                    finishLockScreenActivity()
                }
            }
        }
    }


    private fun isKeyguardLocked(context: Context): Boolean {
        val km =
            (context.applicationContext.getSystemService(Context.KEYGUARD_SERVICE)) as KeyguardManager
        if (km.isDeviceLocked)
            return km.isKeyguardLocked
        else
            return true
    }

    /**
     * Display Activity lock screen
     */
    private fun startLockScreen(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (Settings.canDrawOverlays(context) && PrefRepository.SettingInfo.useLockScreen) {
            goToLockScreen(context)
        }
    }


    private fun goToLockScreen(context: Context) {
        if (!PhoneStateReceiver.isCalling) {
            val intent = Intent(context, LockScreenActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                )
            }

            context.startActivity(intent)
        }
    }

    private fun finishLockScreenActivity() {
        try {
            if (LockScreenActivity.instance != null) {
                LockScreenActivity.instance?.finish()
                LockScreenActivity.instance = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        var calledWhenOff = false
    }
}