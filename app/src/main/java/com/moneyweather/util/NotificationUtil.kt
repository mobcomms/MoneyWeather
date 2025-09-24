package com.moneyweather.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.moneyweather.R

class NotificationUtil {
    companion object {
        fun createNotificationChannel(context: Context?) {
            context?.let { context ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channelId = context.getString(R.string.app_fcm_channel_id)
                    val channelName = context.getString(R.string.app_fcm_channel_name)

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val channel = NotificationChannel(channelId,
                        channelName,
                        NotificationManager.IMPORTANCE_LOW)
                    channel.enableVibration(false)
                    channel.enableLights(false)
                    channel.setShowBadge(false)
                    notificationManager.createNotificationChannel(channel)
                }
            }
        }

        fun createAlarmChannel(context: Context?) {
            context?.let { context ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    val channelId = context.getString(R.string.app_fcm_channel_push_id)
                    val channelName = context.getString(R.string.app_fcm_channel_push_name)
                    val channel = NotificationChannel(channelId,
                        channelName,
                        NotificationManager.IMPORTANCE_HIGH)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        channel.setAllowBubbles(true)
                    }
                    notificationManager.createNotificationChannel(channel)
                }
            }
        }
    }
}