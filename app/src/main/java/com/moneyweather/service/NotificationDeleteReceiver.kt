package com.moneyweather.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class NotificationDeleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (LockScreenService.serviceIntent == null) {
            LockScreenService.serviceIntent = Intent(context, LockScreenService::class.java)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(LockScreenService.serviceIntent)
        } else {
            context.startService(LockScreenService.serviceIntent)
        }
    }
}