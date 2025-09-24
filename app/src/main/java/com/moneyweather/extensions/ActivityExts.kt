package com.moneyweather.extensions

import android.app.Activity
import android.app.AppOpsManager
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.os.Process.myUid
import android.view.WindowManager

fun Activity.allowDisplayOnLockScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(true)

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        // 패턴/PIN/비밀번호/지문/얼굴인식 이 아닌 경우에만 잠금화면을 해제할 수 있음.
        if (!keyguardManager.isDeviceSecure) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (keyguardManager.isKeyguardLocked) {
                    keyguardManager.requestDismissKeyguard(this, null)
                }
            } else {
                // API 27에서는 requestDismissKeyguard()가 없음
                keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE).disableKeyguard()
            }
        }
    } else {
        // FLAG_SHOW_WHEN_LOCKED : 잠금 화면 위에 Activity 표시
        // FLAG_DISMISS_KEYGUARD : 잠금 해제(키가드 해제) 허용
        // FLAG_KEEP_SCREEN_ON : 화면이 꺼지지 않도록 유지

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }
}

fun Activity.isScreenOn(): Boolean = (getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive

fun Activity.hasUsageStatsPermission(): Boolean {
    val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            myUid(),
            packageName
        )
    } else {
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            myUid(),
            packageName
        )
    }

    return AppOpsManager.MODE_ALLOWED == mode
}