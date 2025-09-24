package com.moneyweather.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager

class PhoneStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val tm = context.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager

        when (tm.callState) {
            TelephonyManager.CALL_STATE_RINGING -> {
                context.applicationContext.sendBroadcast(Intent(LockScreenService.PHONE_STATE_BROADCAST))
                isCalling = true
            }

            TelephonyManager.CALL_STATE_OFFHOOK -> {
                context.applicationContext.sendBroadcast(Intent(LockScreenService.PHONE_STATE_BROADCAST_OFF_HOOK))
                isCalling = true
            }

            TelephonyManager.CALL_STATE_IDLE -> {
                Handler(Looper.myLooper()!!).postDelayed({
                    isCalling = false
                    context.applicationContext.sendBroadcast(Intent(LockScreenService.PHONE_STATE_BROADCAST_CLOSE))
                }, 100)
            }
        }
    }

    companion object {
        @JvmStatic
        var isCalling = false
    }

}

