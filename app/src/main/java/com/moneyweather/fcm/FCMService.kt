package com.moneyweather.fcm

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.moneyweather.R
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.model.enums.ActivityEnum
import com.moneyweather.util.PrefRepository
import com.moneyweather.view.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var apiUserModel: ApiUserModel

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        sendRegistrationToCache(token)
        sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        createExpandableNotification(remoteMessage)
    }

    /**
     * @param remoteMessage
     */
    private fun createExpandableNotification(remoteMessage: RemoteMessage) {
        try {
            val channelId = "Donsee"
            val channelName = "DonseeNotifications"
            val smallIcon = R.drawable.ic_noti_light
            val largeImage = getBitmapFromUrl(remoteMessage.data["image"]) ?: null
            val title = remoteMessage.data["title"] ?: ""
            val content = remoteMessage.data["body"] ?: ""

            val intent = getLandingType(remoteMessage)
            val pendingIntent = PendingIntent.getActivity(
                this, Random().nextInt(10000), intent, PendingIntent.FLAG_IMMUTABLE
            )

            // 알림 채널 생성
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (notificationManager.getNotificationChannel(channelId) == null) {
                    val channel = NotificationChannel(
                        channelId,
                        channelName,
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 500, 1000)
                        setSound(
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        setShowBadge(false)
                    }
                    notificationManager.createNotificationChannel(channel)
                }
            }

            // 알림 빌더 생성
            val builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            if (largeImage != null) {
                builder.setStyle(
                    NotificationCompat.BigPictureStyle() // 확장시 설정
                        .bigLargeIcon(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
                        .bigPicture(largeImage)
                        .setSummaryText(content)
                )
                builder.setLargeIcon(largeImage)
            }

            // 알림 표시
            with(NotificationManagerCompat.from(this)) {
                if (ActivityCompat.checkSelfPermission(
                        this@FCMService,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                notify(1001, builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * @param remoteMessage
     * @return
     */
    private fun getLandingType(remoteMessage: RemoteMessage): Intent {
        var intent = Intent(this, MainActivity::class.java)

        try {
            val linkType = remoteMessage.data["link_type"]
            val service = remoteMessage.data["service"]
            val link = remoteMessage.data["link"]

            intent.apply {
                linkType?.let {
                    when (LinkType.fromType(linkType)) {
                        LinkType.HOME -> {
                            putExtra("move_activity", ActivityEnum.NONE)
                            putExtra("link", link)
                        }

                        LinkType.DEEP_LINK -> {
                            service?.let {
                                when (ServiceType.fromCode(service)) {
                                    ServiceType.NOTICE -> {
                                        putExtra("move_activity", ActivityEnum.NOTICE_DETAIL)
                                        putExtra("link", link)
                                    }

                                    ServiceType.FAQ -> {
                                        putExtra("move_activity", ActivityEnum.SETTING)
                                        putExtra("service_type", ServiceType.FAQ)
                                    }

                                    ServiceType.SHOPLUS -> putExtra("move_activity", ActivityEnum.SHOPLUS)
                                    ServiceType.GAME -> putExtra("move_activity", ActivityEnum.GAMEZONE)
                                    null -> putExtra("move_activity", ActivityEnum.NONE)
                                }
                            }
                        }

                        LinkType.OUT_LINK -> {
                            putExtra("move_activity", ActivityEnum.EXTERNAL)
                            putExtra("link", link)
                        }

                        null -> putExtra("move_activity", ActivityEnum.NONE)
                    }
                }

                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return intent
    }

    /**
     * @param imageUrl
     * @return
     */
    private fun getBitmapFromUrl(imageUrl: String?): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * @param uri
     * @return
     */
    private fun getBitmapFromUri(uri: Uri?): Bitmap? {
        return try {
            BitmapFactory.decodeStream(contentResolver.openInputStream(uri!!))
        } catch (e: Exception) {
            null
        }
    }

    /**
     * FCM token 을 cache 에 저장함.
     *
     * @param token
     *
     */
    private fun sendRegistrationToCache(token: String) {
        PrefRepository.UserInfo.fcmToken = token
    }

    /**
     * 로그인 된 상태에서 FCM token 이 만료된 경우 토큰을 서버에 저장함.
     * 1. FCM 서버에서 기존 토큰이 만료되었거나 무효화된 경우
     * 2. 디바이스에서 Google Play 서비스의 데이터가 삭제된 경우
     * 3. Google Play 서비스가 업데이트되면서 FCM 토큰이 갱신될 때
     *
     * @param token
     *
     */
    @SuppressLint("CheckResult")
    private fun sendRegistrationToServer(token: String) {
        if (!PrefRepository.UserInfo.isLogin) return

        apiUserModel.updateFcmToken(token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                if (response.isSuccessful) {
                    PrefRepository.UserInfo.isFcmTokenUpdated = true
                }
            }, { throwable ->
                throwable.printStackTrace()
            })
    }

    enum class LinkType(val type: String) {
        HOME("0"),
        DEEP_LINK("1"),
        OUT_LINK("2");

        companion object {
            fun fromType(type: String): LinkType? = values().find { it.type == type }
        }
    }

    enum class ServiceType(val code: String, val description: String) {
        NOTICE("N", "공지 상세"),
        FAQ("F", "FAQ"),
        SHOPLUS("S", "쇼핑 적립"),
        GAME("G", "에이닉 게임");

        companion object {
            fun fromCode(code: String): ServiceType? = values().find { it.code == code }
        }
    }
}