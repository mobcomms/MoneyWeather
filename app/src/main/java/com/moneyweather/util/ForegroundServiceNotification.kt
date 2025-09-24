package com.moneyweather.util

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.moneyweather.R
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.extensions.getNotificationTime
import com.moneyweather.extensions.toHtmlText
import com.moneyweather.model.Region
import com.moneyweather.model.Weather
import com.moneyweather.service.LockScreenService.Companion.CHANNEL_ID
import com.moneyweather.service.LockScreenService.Companion.instance
import com.moneyweather.service.NotificationDeleteReceiver
import com.moneyweather.view.SplashActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.Date

object ForegroundServiceNotification {
    /**
     * @param context
     */
    @SuppressLint("CheckResult")
    fun fetchDataAndUpdateNotification(service: Service, apiUserModel: ApiUserModel) {
        val context = service.applicationContext

        apiUserModel.notiWeather(
            PrefRepository.LocationInfo.latitude,
            PrefRepository.LocationInfo.longitude
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                response?.let {
                    PrefRepository.LockQuickInfo.notiWeather = it
                }

                val useOldWeather = response == null || response.getWeather().notiDescription.isNullOrEmpty()
                if (useOldWeather) {
                    showNotification(
                        context,
                        PrefRepository.LockQuickInfo.notiWeather.getWeather(),
                        PrefRepository.LockQuickInfo.notiWeather.getRegion()
                    )
                } else {
                    showNotification(
                        context,
                        response.getWeather(),
                        response.getRegion()
                    )
                }
            }, {
                showNotification(
                    context,
                    PrefRepository.LockQuickInfo.notiWeather.getWeather(),
                    PrefRepository.LockQuickInfo.notiWeather.getRegion()
                )
            })
    }

    fun showNotificationQuickly(service: Service) {
        val context = service.applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = context.getString(R.string.app_fcm_channel_id)
        val channelName = context.getString(R.string.app_fcm_channel_name)
        val titleType = context.getString(R.string.notification_title_type)
        val contentType = context.getString(R.string.notification_content_type)
        val id = CHANNEL_ID

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                enableVibration(false)
                enableLights(false)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val smallIcon = R.drawable.ic_noti_light

        val intent = Intent(context, SplashActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, flags)
        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(titleType)
            .setContentText(contentType)
            .setSmallIcon(smallIcon)
            .setTicker("MoneyWeather Reward")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(channelId)
        }

        safeStartForeground(
            context,
            service,
            id,
            builder.build()
        )
    }

    /**
     * @param context
     * @param weather
     * @param region
     */
    fun showNotification(
        context: Context,
        weather: Weather,
        region: Region
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = context.getString(R.string.app_fcm_channel_id)
        val channelName = context.getString(R.string.app_fcm_channel_name)
        val titleType = context.getString(R.string.notification_title_type)
        val contentType = context.getString(R.string.notification_content_type)
        val id = CHANNEL_ID

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                enableVibration(false)
                enableLights(false)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val smallIcon = R.drawable.ic_noti_light
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val layoutId = if (Configuration.UI_MODE_NIGHT_YES == nightModeFlags) {
            R.layout.notification_widget_dark
        } else {
            R.layout.notification_widget
        }

        val regionMessage = String.format(
            context.getString(R.string.notification_region_message),
            region.getLastDepth(),
            Date().getNotificationTime()
        ).toHtmlText()

        val weatherMessage = weather.run {
            String.format(
                context.getString(R.string.notification_weather_message),
                temp(),
                minTemp(),
                maxTemp(),
                pm25Description
            )
        }

        val notificationLayout = RemoteViews(context.packageName, layoutId).apply {
            setTextViewText(R.id.region, regionMessage)
            setTextViewText(R.id.weather, weatherMessage)
            setTextViewText(R.id.weatherDesc, weather.notiDescription)
            setImageViewBitmap(R.id.wImg, weather.weatherImage()?.toBitmap())
        }

        val intent = Intent(context, SplashActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val deleteIntent = Intent(context, NotificationDeleteReceiver::class.java)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, flags)
        val deletePendingIntent = PendingIntent.getBroadcast(context, 1, deleteIntent, flags)

        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(titleType)
            .setContentText(contentType)
            .setCustomContentView(notificationLayout)
            .setSmallIcon(smallIcon)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setTicker("MoneyWeather Reward")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(false)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(deletePendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(channelId)
        }

        safeStartForeground(
            context,
            instance,
            id,
            builder.build()
        )
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun getFgsType(): Int {
        val isForeground = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        return if (isForeground) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE or ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        } else {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        }
    }

    @SuppressLint("WrongConstant")
    private fun safeStartForeground(
        context: Context,
        service: Service?,
        notificationId: Int,
        notification: Notification
    ) {
        service ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val isGrantedLocationPermission = PermissionUtils.isGrantedPermission(
                context, SplashActivity.REQUEST_LOCATION_PERMISSIONS
            )

            val isGrantedServicePermission = PermissionUtils.isGrantedPermission(
                context, SplashActivity.REQUEST_SERVICE_PERMISSION
            )

            if (!isGrantedLocationPermission || !isGrantedServicePermission) return

            showAndRemoveOverlay(context) {
                service.startForeground(
                    notificationId,
                    notification,
                    getFgsType()
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            service.startForeground(notificationId, notification)
        } else {
            val notificationManager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, notification)
        }
    }

    private fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    private fun showAndRemoveOverlay(context: Context, onVisibleOrNotHasPermission: () -> Unit) {
        if (!hasOverlayPermission(context)) {
            onVisibleOrNotHasPermission()
            return
        }

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val overlayView = View(context).apply {
            setBackgroundColor(Color.TRANSPARENT)

            // View 가시성 감지용 콜백 설정 (Android 15 대응)
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    if (v.windowVisibility == View.VISIBLE) {
                        // 오버레이가 실제로 표시됨 → 서비스 시작 가능
                        onVisibleOrNotHasPermission()

                        // 서비스 시작 후 뷰 제거
                        Handler(Looper.getMainLooper()).postDelayed({
                            wm.removeView(v)
                        }, 500)
                    }
                }

                override fun onViewDetachedFromWindow(v: View) {}
            })
        }

        val params = WindowManager.LayoutParams(
            1, 1,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        wm.addView(overlayView, params)
    }
}