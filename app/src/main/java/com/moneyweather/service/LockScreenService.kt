package com.moneyweather.service

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.os.IBinder
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.util.ForegroundServiceNotification.fetchDataAndUpdateNotification
import com.moneyweather.util.ForegroundServiceNotification.showNotificationQuickly
import com.moneyweather.util.PrefRepository
import com.moneyweather.view.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class LockScreenService : Service() {
    private var mReceiver: BroadcastReceiver? = null
    private var mAlarmManager: AlarmManager? = null

    @Inject
    lateinit var apiUserModel: ApiUserModel

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        registerReceiver()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceIntent = intent

        showNotificationQuickly(this)
        fetchDataAndUpdateNotification(this, apiUserModel)

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)

        setAlarmTimer()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        fetchDataAndUpdateNotification(this, apiUserModel)
    }

    override fun onDestroy() {

        try {
            CoroutineScope(Dispatchers.IO).launch {
                cacheDir.deleteRecursively()
            }
        } catch (e: Exception) {
        }

        super.onDestroy()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        unRegisterReceiver()
        serviceIntent = null

        // 서비스가 강제 중단된 경우에는 onDestroy 함수가 호출되지 않아 사실상 아래 로직은 의미없음
        if (mAlarmManager == null && PrefRepository.SettingInfo.useLockScreen) {
            setAlarmTimer()
        }
    }

    private fun getClickIntent(type: Int): Intent? {
        val intent: Intent
        //        if (MyApplication.isFinish) {
//            intent = new Intent(getApplicationContext(), SplashActivity.class);
//        } else {
        intent = Intent(applicationContext, MainActivity::class.java)
        //        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        //  intent.putExtra("isSignUp", true);
        intent.action = "action_$type"
        when (type) {
            0 -> intent.putExtra("move_activity", "point")
            1 -> intent.putExtra("move_activity", "time")
            2 -> intent.putExtra("move_activity", "news")
            3 -> intent.putExtra("move_activity", "setting")
        }
        return intent
    }

    /**
     * 리시버 등록
     */
    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF) // 스크린 OFF
            addAction(Intent.ACTION_SCREEN_ON) // 스크린 ON
            addAction(Intent.ACTION_TIME_CHANGED) // 시간 강제 변경
            addAction(Intent.ACTION_TIMEZONE_CHANGED) // 타임존 강제 변경
            addAction(Intent.ACTION_DATE_CHANGED) // 날짜 변경
            addAction(Intent.ACTION_SHUTDOWN)
            addAction(PHONE_STATE_BROADCAST)
            addAction(PHONE_STATE_BROADCAST_CLOSE)
            addAction(USER_STATE_LOGIN)
            addAction(USER_STATE_USELOCKSCREEN)
            addAction(USER_STATE_TOKEN)

            priority = IntentFilter.SYSTEM_HIGH_PRIORITY
        }

        if (mReceiver == null) {
            mReceiver = LockScreenReceiver()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(mReceiver, filter, RECEIVER_EXPORTED)
        } else
            registerReceiver(mReceiver, filter)
    }

    /**
     * 리시버 해제
     */
    private fun unRegisterReceiver() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver)
            mReceiver = null
        }
    }

    /**
     * 서비스 재시작 알림 매니저
     */
    private fun setAlarmTimer() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.MILLISECOND, 500)
        val intent = Intent(this, RebootAlarmReceiver::class.java)
        val flags: Int = PendingIntent.FLAG_MUTABLE
        val sender: PendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags)
        mAlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        /**
         * setExactAndAllowWhileIdle은 API 레벨 23 이상에서만 구동됨
         * setExactAndAllowWhileIdle 또는 setExact을 사용하려면
         * Android 12(API 레벨 31) 이상부터 SCHEDULE_EXACT_ALARM 권한 필요
         * 권한을 넣어서 처리하던가 아니면 그냥 setAndAllowWhileIdle로 처리
         */
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mAlarmManager?.setExactAndAllowWhileIdle(
//                AlarmManager.RTC_WAKEUP,
//                calendar.timeInMillis,
//                sender
//            )
//        } else {
        mAlarmManager?.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            sender
        )
//        }
    }

    companion object {
        @JvmStatic
        var serviceIntent: Intent? = null

        var instance: LockScreenService? = null

        var CHANNEL_ID: Int = 1122332211

        const val PHONE_STATE_BROADCAST = "CP.PHONE_STATE_BROADCAST"
        const val PHONE_STATE_BROADCAST_OFF_HOOK = "CP.PHONE_STATE_BROADCAST_OFF_HOOK"
        const val PHONE_STATE_BROADCAST_CLOSE = "CP.PHONE_STATE_BROADCAST_CLOSE"

        const val USER_STATE_LOGIN = "CP.USER_STATE_LOGIN"
        const val USER_STATE_USELOCKSCREEN = "CP.USER_STATE_USELOCKSCREEN"
        const val USER_STATE_TOKEN = "CP.USER_STATE_TOKEN"
    }
}