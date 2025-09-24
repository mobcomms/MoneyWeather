package com.moneyweather.util

import android.text.TextUtils
import com.moneyweather.R
import com.moneyweather.base.BaseApplication
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtil {

    private const val FORMAT_BASIC_DATE_TIME = "yyyyMMddHHmmss"
    private const val FORMAT_DISPLAY_DATE_TIME = "yyyy.MM.dd HH:mm:ss"
    private const val FORMAT_LOCKSCREEN_DATE_TIME = "MM월 dd일 (E) hh:mm"
    private const val FORMAT_BACKGROUND_THEME_DATE_TIME = "MM/dd (E) hh:mm"

    fun getNowDateString(pattern: String = FORMAT_LOCKSCREEN_DATE_TIME): String {
        var date = ""
        try {
            val formater = SimpleDateFormat(pattern, Locale.KOREA)
            date = formater.format(Date())
        } catch (e: java.lang.Exception) {

        }
        return date
    }

    fun getBackgroundThemeDateString(): String =
        SimpleDateFormat(FORMAT_BACKGROUND_THEME_DATE_TIME, Locale.KOREA).format(Date())

    fun strToDate(value: String, pattern: String = FORMAT_BASIC_DATE_TIME): Date? {
        var rst: Date? = null
        try {
            val formatter = SimpleDateFormat(pattern)
            rst = formatter.parse(value)
            return rst
        } catch (e: Exception) {
        }
        return rst
    }

    @JvmStatic
    fun getDayofWeek(value: Int): Boolean {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        val dayOfWeekNumber = calendar[Calendar.DAY_OF_WEEK]

        return value == dayOfWeekNumber
    }

    @JvmStatic
    fun getHourToString(value: Int): String {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.HOUR_OF_DAY, value)
        val nextTime = calendar.get(Calendar.HOUR_OF_DAY)
        return nextTime.toString().plus(BaseApplication.appContext().getString(R.string.hour))
    }

    fun getNowMin(): Int {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        return calendar[Calendar.MINUTE]
    }

    fun getNowSecond(): Int {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        return calendar[Calendar.SECOND]
    }

    fun strToFormattedDateString(
        value: String,
        pattern: String = FORMAT_BASIC_DATE_TIME,
        returnPattern: String
    ): String? {
        var date: Date? = null
        try {
            val formatter = SimpleDateFormat(FORMAT_BASIC_DATE_TIME)
            date = formatter.parse(value)

            return dateToStr(date, returnPattern)
        } catch (e: Exception) {
        }
        return null
    }

    fun dateToStr(date: Date, pattern: String = FORMAT_DISPLAY_DATE_TIME): String {
        var sDate = ""
        try {
            val formater = SimpleDateFormat(pattern, Locale.KOREA)
            sDate = formater.format(date)
        } catch (e: java.lang.Exception) {
            Logger.e("dateToStr Exception!", e.message)
        }
        return sDate
    }

    fun incMinute(date: Date?, value: Int?): Date? {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.MINUTE, value!!)
        return calendar.time
    }

    //    하루 지나면 팝업 보여짐
    fun getData(): Boolean {
        val sampleDate = PrefRepository.LockQuickInfo.mainPopupDate

        if (!TextUtils.isEmpty(sampleDate)) {
            try {
                val sf = SimpleDateFormat("yyyy-MM-dd")
                val today = Calendar.getInstance()
                val date = sf.parse(sampleDate)
                val calDate = today.time.time - date.time
                val calcuDate = calDate / (60 * 60 * 24 * 1000)
                return calcuDate.toInt() != 0
            } catch (e: ParseException) {
                e.printStackTrace()
                return true
            }
        }
        return true
    }

    /**
     * 배터리 최적화 팝업 노출 여부 판별값
     */
    val isBatteryOptimizationShow: Boolean
        get() {
            // 저장된 시간
            val savedTime = PrefRepository.SettingInfo.batteryOptimizationSaveTime
            val currentTime = Calendar.getInstance().timeInMillis
            val diff = (currentTime - savedTime) / (60 * 60 * 1000)
            return diff >= 12
        }

    /**
     * 락스크린 잠시끄기 판별
     */
    val isLockScreenTime: Boolean
        get() {
            val rockScreenApiTime = PrefRepository.LockQuickInfo.rockScreenTimeCheck
            val rockScreenApiTime2 = PrefRepository.LockQuickInfo.rockScreenTimeCheck2
            val rockScreenTimeClear = PrefRepository.LockQuickInfo.rockScreenTimeClear
            val rockScreenTimeOffCode = PrefRepository.LockQuickInfo.rockScreenTimeOffCode

            if (rockScreenTimeOffCode == 5) {
                return false
            }

            if (!TextUtils.isEmpty(rockScreenTimeClear)) {
                try {
                    val sf = SimpleDateFormat("yyyy-MM-dd")
                    val today = Calendar.getInstance()
                    val date = sf.parse(rockScreenApiTime2)
                    val calDate = today.time.time - date.time
                    val calcuDate = calDate / (60 * 60 * 24 * 1000)
                    return if (calcuDate.toInt() != 0) {
                        calcuDate.toInt() != 0
                    } else {
                        // 저장된 시간
                        val currentTime = Calendar.getInstance().timeInMillis
                        val diff = (currentTime - rockScreenApiTime) / (60 * 60 * 1000)
                        diff >= rockScreenTimeClear.toInt()
                    }

                } catch (e: ParseException) {
                    e.printStackTrace()
                    return true
                }
            }
            return true
        }

    fun getTime(): String {
        var now = System.currentTimeMillis()
        var date = Date(now)

        var dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var getTime = dateFormat.format(date)

        return getTime
    }

    fun intervalBetweenDateText(beforeDate: String): Int {
        val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(getTime())
        val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(beforeDate)
        val diffMilliseconds = nowFormat.time - beforeFormat.time
        val diffMinutes = diffMilliseconds / (60 * 1000)
       return diffMinutes.toInt()
    }
}