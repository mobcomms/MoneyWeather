package com.moneyweather.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Patterns
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.moneyweather.R
import com.moneyweather.base.BaseApplication
import com.moneyweather.extensions.requestApplyInsetsWhenAttached
import com.moneyweather.model.AppInfo
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.io.File
import java.net.URLEncoder
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CommonUtils {
    companion object {
        interface OnCallbackAdidListener {
            fun onCallbackADID(adid: String?)
        }

        interface OnCallbackLocationListener {
            fun onCallbackLocation(lat: String, lon: String)
        }

        private val files = arrayOf(
            "/sbin/su",
            "/system/su",
            "/system/bin/su",
            "/system/sbin/su",
            "/system/xbin/su",
            "/system/xbin/mu",
            "/system/bin/.ext/.su",
            "/system/usr/su-backup",
            "/data/data/com.noshufou.android.su",
            "/system/app/Superuser.apk",
            "/system/app/su.apk",
            "/system/bin/.ext",
            "/system/xbin/.ext"
        )

        fun getAppVersion(): String? {
            var version = ""
            val context: Context = BaseApplication.appContext()
            try {
                val i = context.packageManager.getPackageInfo(context.packageName, 0)
                version = i.versionName ?: ""
            } catch (var3: PackageManager.NameNotFoundException) {
                Timber.e("getAppVersion() Exception! $var3")
            }
            return version
        }

        fun getAdId(context: Context?, onCallbackAdidListener: OnCallbackAdidListener?) {
            object : Thread() {
                override fun run() {
                    var adid: String? = null
                    try {
                        val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(
                            context!!
                        )
                        adid = adInfo.id

//                    PreferencesUtil.getInstance().putString(Key.KEY_AD_ID, adid);
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    adid = AdIdUtils.convertToValidADID(adid)
                    onCallbackAdidListener?.onCallbackADID(adid)
                }
            }.start()
        }

        fun getDeviceId(context: Context): String {
            var deviceId: String? = ""
            deviceId =
                Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
            Logger.d("deviceId : $deviceId")

            return deviceId
        }


        /**
         * 필수 퍼미션 체크
         */
        fun isPermissionDenied(context: Context?): Boolean {

            val isDenied: Boolean =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val isPhonePermission = (ActivityCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED)

                    !isPhonePermission
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val isPhonePermission = (ActivityCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED)

                    !isPhonePermission
                } else {
                    (ActivityCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.READ_PHONE_STATE
                    ) != PackageManager.PERMISSION_GRANTED)
                }
//        Logger.i("version " + Build.VERSION.SDK_INT)
//        Logger.i("isDenied $isDenied")
//        Logger.i("READ_PHONE_STATE " + ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE))
//        Logger.i("READ_PHONE_NUMBERS " + ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS))
//        Logger.i("ACTIVITY_RECOGNITION " + ActivityCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION))
            return isDenied
        }

        fun isValidEmail(email: String): Boolean {
            val pattern = Patterns.EMAIL_ADDRESS
            return pattern.matcher(email).matches()
        }

        fun isValidPassword(pass: String): Boolean {
            val passRegex = "^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z0-9]{8,32}$"
            return pass.matches(passRegex.toRegex())
        }

        fun getDisplayWidth(context: Context): Int {
            val dm: DisplayMetrics = context.getResources().getDisplayMetrics()
            return dm.widthPixels
        }

        fun getDisplayHeight(context: Context): Int {
            val dm: DisplayMetrics = context.getResources().getDisplayMetrics()
            return dm.heightPixels
        }

        @JvmStatic
        fun getCommaNumeric(value: Float): String {
            if (value > 0) {
                val format = DecimalFormat("###,###.#")
                val `val` = format.format(value.toDouble())
                return if (`val`.endsWith(".0")) StringUtils.removeEnd(
                    format.format(value.toDouble()),
                    ".0"
                )
                else `val`
            } else {
                // .0일때 '0'으로 보이게 수정
                return "0"
            }
        }

        fun dpToPx(context: Context, dp: Int): Int {
            val density = context.resources.displayMetrics.density
            return Math.round(dp.toFloat() * density)
        }

        fun isListEmpty(list: ArrayList<*>?): Boolean {
            return list == null || list.isEmpty()
        }

        fun newDateFormat(value: String): String {
            val getFormat = SimpleDateFormat("yyyyMMddHHmmss")
            try {
                val getDate = getFormat.parse(value)
                val transFormat = SimpleDateFormat("yyyy. MM. dd HH:mm:ss")
                return transFormat.format(getDate)
            } catch (e: ParseException) {
                val year = value.substring(0, 4)
                val month = value.substring(4, 6)
                val date = value.substring(6, 8)
                val hour = value.substring(8, 10)
                val minute = value.substring(10, 12)
                val second = value.substring(12, 14)
                return "$year. $month. $date $hour:$minute:$second"
            }
        }

        /**
         * 위치 설정 상태 반환
         * @param context
         * @return
         */
        fun isLocationEnabled(context: Context): Boolean {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }

        @SuppressLint("MissingPermission")
        fun getLocation(
            onSuccessCallbackListener: OnCallbackLocationListener? = null,
            onFailureCallbackListener: OnCallbackLocationListener? = null
        ) {

            val fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(BaseApplication.appContext())

            fusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    if (location == null) {
                        onFailureCallbackListener?.onCallbackLocation(
                            PrefRepository.LocationInfo.latitude,
                            PrefRepository.LocationInfo.longitude
                        )
                    }

                    location?.let {
                        PrefRepository.LocationInfo.latitude = it.latitude.toString()
                        PrefRepository.LocationInfo.longitude = it.longitude.toString()

                        onSuccessCallbackListener?.onCallbackLocation(
                            it.latitude.toString(),
                            it.longitude.toString()
                        )
                    }
                }
                .addOnFailureListener { e ->
                    onFailureCallbackListener?.onCallbackLocation(
                        PrefRepository.LocationInfo.latitude,
                        PrefRepository.LocationInfo.longitude
                    )
                }

        }

        fun vibrate(context: Context) {
            if (!PrefRepository.SettingInfo.useSavePointVibration) return

            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val milliseconds = 100L
            val amplitude = 1

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, amplitude))
            } else {
                vibrator.vibrate(milliseconds)
            }
        }

        fun sound(context: Context) {
            var player = MediaPlayer.create(context, R.raw.coin_sound_2)
            player.setVolume(0.2f, 0.2f)
            player.start()
        }

        fun getWeatherSearchUrl(context: Context): String {
            var query = StringBuilder()
            AppInfo.regionInfo?.value?.apply {
                query.append(depth2).append(" ")

                if (StringUtils.isNotEmpty(depth3)) {
                    query.append(depth3).append(" ")
                }
            }
            query.append(context.getString(R.string.weather))

            var prevUrl = PrefRepository.SettingInfo.externalWeatherSearchUrl
            var queryStr = URLEncoder.encode(query.toString(), "UTF-8")
            var result = prevUrl + queryStr

            return result
        }

        fun getScreenWidth(context: Context): Int {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = wm.currentWindowMetrics
                val insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                windowMetrics.bounds.width() - insets.left - insets.right
            } else {
                val displayMetrics = DisplayMetrics()
                wm.defaultDisplay.getMetrics(displayMetrics)
                displayMetrics.widthPixels
            }
        }

        fun getScreenHeight(context: Context): Int {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = wm.currentWindowMetrics
                val insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                windowMetrics.bounds.height() - insets.bottom - insets.top
            } else {
                val displayMetrics = DisplayMetrics()
                wm.defaultDisplay.getMetrics(displayMetrics)
                displayMetrics.heightPixels
            }
        }

        fun setActivitySystemBarPadding(rootView: View) {
            // 15 버전 대응 코드 (Edge-to-edge 화면 사용으로 인한 패딩 값 적용)
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
                val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(
                    systemBarsInsets.left,
                    systemBarsInsets.top,
                    systemBarsInsets.right,
                    systemBarsInsets.bottom
                )

                insets
            }
        }

        fun removeSystemBarPadding(rootView: View) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, null)
        }

        fun setLockScreenSystemBarPaddingAndColor(activity: Activity, rootView: View, @ColorRes colorResId: Int, isLight: Boolean) {
            val window = activity.window

            // Activity에 자동 inset 적용 중단
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // fragment root view 에 inset 수동 적용
            if (isLight) {
                ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
                    val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    view.setPadding(
                        systemBarsInsets.left,
                        systemBarsInsets.top,
                        systemBarsInsets.right,
                        systemBarsInsets.bottom
                    )
                    insets
                }
            }

            // Insets 강제 적용
            rootView.requestApplyInsetsWhenAttached()

            // activity root view 에 inset 수동 적용
            val activityRootView: View = activity.findViewById(android.R.id.content)
            ViewCompat.setOnApplyWindowInsetsListener(activityRootView) { view, insets ->
                val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                view.findViewById<View>(R.id.status_bar_spacer).apply {
                    updateLayoutParams<LinearLayout.LayoutParams> {
                        height = if (isLight) 0 else systemBarsInsets.top
                    }

                    setBackgroundColor(ContextCompat.getColor(activity, colorResId))
                }

                view.findViewById<View>(R.id.navigation_bar_spacer).apply {
                    updateLayoutParams<LinearLayout.LayoutParams> {
                        height = if (isLight) 0 else systemBarsInsets.bottom
                    }
                    setBackgroundColor(ContextCompat.getColor(activity, colorResId))
                }

                insets
            }

            // Insets 강제 적용
            activityRootView.requestApplyInsetsWhenAttached()

            // 상태바, 네비게이션바 배경색 설정
            window.statusBarColor = ContextCompat.getColor(activity, colorResId)
            window.navigationBarColor = ContextCompat.getColor(activity, colorResId)

            // 상태바, 네비게이션바 아이콘 색상 설정
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isLight
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = isLight
        }

        /**
         * @param context
         * @return BatteryLevel
         */
        fun getBatteryLevel(context: Context): String {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            }

            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = level / scale.toFloat() * 100

            return "${batteryPct.toInt()}%"
        }

        fun isEmulator() = (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("sdk_gphone64_arm64")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")

        fun checkSuperUser() = checkRootedFiles() || checkSuperUserCommand()

        private fun checkRootedFiles(): Boolean {
            for (i in files.indices) {
                val file = File(files[i])
                if (null != file && file.exists()) {
                    return true
                }
            }
            return false
        }

        private fun checkSuperUserCommand(): Boolean {
            try {
                Runtime.getRuntime().exec("su")
                return true
            } catch (e: Exception) {
                return false
            }
        }

        /**
         * @return
         */
        fun getCurrentDate(): String {
            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            return dateFormat.format(Calendar.getInstance().time)
        }

        /**
         * @return
         */
        fun getCurrentDate2(): String {
            val dateFormat = SimpleDateFormat("yyyyMMddhh", Locale.getDefault())
            return dateFormat.format(Calendar.getInstance().time)
        }

        /**
         * @param savedDate
         * @return
         */
        fun shouldShowContent(savedDate: String) = savedDate != getCurrentDate()
    }
}