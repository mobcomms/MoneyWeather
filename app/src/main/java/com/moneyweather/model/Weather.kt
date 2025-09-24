package com.moneyweather.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.moneyweather.R
import com.moneyweather.base.BaseApplication
import com.moneyweather.model.enums.VideoBgType
import com.moneyweather.model.enums.WeatherType
import com.moneyweather.util.WeatherUtils
import kotlin.math.abs

data class Weather(
    val forecastDay: Int? = 0,
    val forecastTime: String? = "",
    val temp: Float = 0f,
    val minTemp: Float = 0f,
    val maxTemp: Float = 0f,
    val skyDescription: String = "",
    val skyCode: Int = 0,
    val rainAmount: Float? = 0f,
    val rainPercent: Float? = 0f,
    val rainDescription: String? = "",
    val snowDescription: String? = "",
    val windDirection: Float? = 0f,
    val windSpeed: Float? = 0f,
    val humidity: Int? = 0,
    val windDirectionCode: String? = "",
    val windDirectionDesc: String? = "",
    val pm10Amount: Int? = 0,
    val pm10Description: String? = "",
    val pm25Amount: Int? = 0,
    val pm25Description: String? = "",
    val uvLevel: Int? = 0,
    val uvDescription: String? = "",
    val yesterdayTemp: Float = 0f,
    val apparentTemp: Float? = 0f,
    val hour: Int? = 0,
    val notiDescription: String? = "",
    val sunrise: String? = "",
    val sunset: String? = "",
    val tomorrowSunrise: String? = "",
) {
    fun sunrise() = formattedString(sunrise!!)
    fun sunset() = formattedString(sunset!!)
    fun tomorrowSunrise() = formattedString(tomorrowSunrise!!)

    private fun formattedString(str: String) = try {
        str?.replace(Regex("(\\d{2})(\\d{2})"), "$1:$2")
    } catch (e: Exception) {
        str
    }

    @SuppressLint("DefaultLocale")
    fun condition(): String? {
        val compare = temp - yesterdayTemp
        val context = BaseApplication.appContext()
        val result = if (compare > 0) {
            String.format(
                "%s %.1f%s %s / %s",
                context.getString(R.string.than_yesterday),
                Math.abs(compare),
                context.getString(R.string.degrees_han),
                context.getString(R.string.high),
                skyDescription
            )
        } else if (compare < 0) {
            String.format(
                "%s %.1f%s %s / %s",
                context.getString(R.string.than_yesterday),
                Math.abs(compare),
                context.getString(R.string.degrees_han),
                context.getString(R.string.low),
                skyDescription
            )
        } else {
            String.format("%s / %s", context.getString(R.string.same_yesterday), skyDescription)
        }
        return result
    }

    fun lockCondition(): String? {

        val compare = temp - yesterdayTemp
        val context = BaseApplication.appContext()
        val result = if (compare > 0) {
            String.format(context.getString(R.string.than_yesterday_more), Math.abs(compare), context.getString(R.string.temp_high))
        } else if (compare < 0) {
            String.format(context.getString(R.string.than_yesterday_more), Math.abs(compare), context.getString(R.string.temp_low))
        } else {
            context.getString(R.string.than_yesterday_same)
        }
        return result
    }

    fun backgroundThemeCondition(): String? {
        val compare = temp - yesterdayTemp
        val context = BaseApplication.appContext()

        return if (compare > 0) {
            String.format(context.getString(R.string.than_yesterday_more2), abs(compare), context.getString(R.string.high_arrow))
        } else if (compare < 0) {
            String.format(context.getString(R.string.than_yesterday_more2), abs(compare), context.getString(R.string.low_arrow))
        } else {
            context.getString(R.string.than_yesterday_same)
        }
    }

    fun lockCalendarCondition(): String? {

        val compare = temp - yesterdayTemp
        val context = BaseApplication.appContext()
        val result = if (compare > 0) {
            String.format(context.getString(R.string.than_yesterday_more2), Math.abs(compare), context.getString(R.string.high_arrow))
        } else if (compare < 0) {
            String.format(context.getString(R.string.than_yesterday_more2), Math.abs(compare), context.getString(R.string.low_arrow))
        } else {
            context.getString(R.string.than_yesterday_same2)
        }
        return result
    }

    fun lockSimpleCondition(): String? {

        val compare = temp.toInt() - yesterdayTemp.toInt()
        val context = BaseApplication.appContext()
        val result = if (compare > 0) {
            String.format(context.getString(R.string.than_yesterday_more3), Math.abs(compare), context.getString(R.string.high))
        } else if (compare < 0) {
            String.format(context.getString(R.string.than_yesterday_more3), Math.abs(compare), context.getString(R.string.low))
        } else {
            context.getString(R.string.than_yesterday_same2)
        }
        return result
    }

    fun lockSimpleWeather(): SpannableStringBuilder? {
        val context = BaseApplication.appContext()
        var first_str = String.format("%s %d%s ", skyDescription, temp.toInt(), context.getString(R.string.degrees))
        var last_str = String.format("(%d%s/%d%s)", minTemp.toInt(), context.getString(R.string.degrees), maxTemp.toInt(), context.getString(R.string.degrees))

        val spannableString = SpannableString(first_str)
        val builder = SpannableStringBuilder(spannableString)
        builder.append(last_str)
        val begin = first_str.length
        val end = builder.length

        builder.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.grey_74)),
            begin,
            end,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        return builder
    }

    fun hour(): String? {
        return hour.toString().plus(BaseApplication.appContext().getString(R.string.hour))
    }

    fun weatherImage(): Drawable? {
        val context = BaseApplication.appContext()
        when (skyCode) {
            WeatherType.CLEAN.type -> return context.getDrawable(R.drawable.icon_weather_1)
            WeatherType.CLOUDY.type -> return context.getDrawable(R.drawable.icon_weather_2)
            WeatherType.CLOUD.type -> return context.getDrawable(R.drawable.icon_weather_3)
            WeatherType.RAIN.type -> return context.getDrawable(R.drawable.icon_weather_4)
            WeatherType.SNOW.type -> return context.getDrawable(R.drawable.icon_weather_5)
            WeatherType.SNOW_RAIN.type -> return context.getDrawable(R.drawable.icon_weather_6)
            WeatherType.SHOWER.type -> return context.getDrawable(R.drawable.icon_weather_7)
            WeatherType.SHOWER_SNOW.type -> return context.getDrawable(R.drawable.icon_weather_8)
            WeatherType.SMOG.type -> return context.getDrawable(R.drawable.icon_weather_9)
            WeatherType.THUNDER.type -> return context.getDrawable(R.drawable.icon_weather_10)
            WeatherType.GRADUALLY_CLOUDY.type -> return context.getDrawable(R.drawable.icon_weather_11)
            WeatherType.CLOUDY_THUNDER.type -> return context.getDrawable(R.drawable.icon_weather_12)
            WeatherType.CLOUDY_RAIN.type -> return context.getDrawable(R.drawable.icon_weather_13)
            WeatherType.CLOUDY_SNOW.type -> return context.getDrawable(R.drawable.icon_weather_14)
            WeatherType.CLOUDY_SNOW_RAIN.type -> return context.getDrawable(R.drawable.icon_weather_15)
            WeatherType.CLOUDY_CLEAN.type -> return context.getDrawable(R.drawable.icon_weather_16)
            WeatherType.THUNDER_CLEAN.type -> return context.getDrawable(R.drawable.icon_weather_17)
            WeatherType.RAIN_CLEAN.type -> return context.getDrawable(R.drawable.icon_weather_18)
            WeatherType.SNOW_CLEAN.type -> return context.getDrawable(R.drawable.icon_weather_19)
            WeatherType.SNOW_RAIN_CLEAN.type -> return context.getDrawable(R.drawable.icon_weather_20)
            WeatherType.CLOUDY_MANY.type -> return context.getDrawable(R.drawable.icon_weather_21)
            WeatherType.DUST_STORM.type -> return context.getDrawable(R.drawable.icon_weather_22)
        }
        return context.getDrawable(R.drawable.icon_weather_1)
    }

    fun weatherBackground(): Drawable? {
        val context = BaseApplication.appContext()
        return when (skyCode) {
            // 맑음
            WeatherType.CLEAN.type,
            WeatherType.CLOUDY.type,
            WeatherType.CLOUDY_CLEAN.type,
            WeatherType.THUNDER_CLEAN.type,
            WeatherType.RAIN_CLEAN.type,
            WeatherType.SNOW_CLEAN.type,
            WeatherType.SNOW_RAIN_CLEAN.type,
            WeatherType.CLOUDY_MANY.type -> {
                if (WeatherUtils.isSunsetGlow(this@Weather)) {
                    context.getDrawable(R.drawable.bg_sunset)
                } else {
                    if (WeatherUtils.checkSunrise(this@Weather)) {
                        context.getDrawable(R.drawable.bg_sunny_day)
                    } else {
                        context.getDrawable(R.drawable.bg_sunny_night)
                    }
                }
            }

            // 눈
            WeatherType.SNOW.type,
            WeatherType.SNOW_RAIN.type,
            WeatherType.SHOWER_SNOW.type,
            WeatherType.CLOUDY_SNOW.type,
            WeatherType.CLOUDY_SNOW_RAIN.type -> {
                if (WeatherUtils.checkSunrise(this@Weather)) {
                    context.getDrawable(R.drawable.bg_snowly_day)
                } else {
                    context.getDrawable(R.drawable.bg_snowly_night)
                }
            }

            // 비
            WeatherType.RAIN.type,
            WeatherType.SHOWER.type,
            WeatherType.THUNDER.type,
            WeatherType.CLOUDY_THUNDER.type,
            WeatherType.CLOUDY_RAIN.type -> {
                if (WeatherUtils.checkSunrise(this@Weather)) {
                    context.getDrawable(R.drawable.bg_rainy_day)
                } else {
                    context.getDrawable(R.drawable.bg_rainy_night)
                }
            }

            // 흐림
            WeatherType.CLOUD.type,
            WeatherType.SMOG.type,
            WeatherType.GRADUALLY_CLOUDY.type,
            WeatherType.DUST_STORM.type -> {
                if (WeatherUtils.checkSunrise(this@Weather)) {
                    context.getDrawable(R.drawable.bg_cloudy_day)
                } else {
                    context.getDrawable(R.drawable.bg_cloudy_night)
                }
            }

            else -> null
        }
    }

    fun weatherVideo(): String {
        when (skyCode) {
            // 맑음
            WeatherType.CLEAN.type,
            WeatherType.CLOUDY.type,
            WeatherType.CLOUDY_CLEAN.type,
            WeatherType.THUNDER_CLEAN.type,
            WeatherType.RAIN_CLEAN.type,
            WeatherType.SNOW_CLEAN.type,
            WeatherType.SNOW_RAIN_CLEAN.type,
            WeatherType.CLOUDY_MANY.type -> return VideoBgType.CLEAR.video

            // 눈
            WeatherType.SNOW.type,
            WeatherType.SNOW_RAIN.type,
            WeatherType.SHOWER_SNOW.type,
            WeatherType.CLOUDY_SNOW.type,
            WeatherType.CLOUDY_SNOW_RAIN.type -> return VideoBgType.SNOWY.video

            // 비
            WeatherType.RAIN.type,
            WeatherType.SHOWER.type,
            WeatherType.THUNDER.type,
            WeatherType.CLOUDY_THUNDER.type,
            WeatherType.CLOUDY_RAIN.type -> return VideoBgType.RAINY.video

            // 구름
            WeatherType.CLOUD.type,
            WeatherType.SMOG.type,
            WeatherType.GRADUALLY_CLOUDY.type,
            WeatherType.DUST_STORM.type -> return VideoBgType.CLOUDY.video

            else -> return VideoBgType.CLEAR.video
        }
    }

    fun temp(): String {
        return String.format("%s%s", temp.toInt(), BaseApplication.appContext().getString(R.string.degrees))
    }

    fun minTemp(): String {
        return String.format("%s%s", minTemp.toInt(), BaseApplication.appContext().getString(R.string.degrees))
    }

    fun maxTemp(): String {
        return String.format("%s%s", maxTemp.toInt(), BaseApplication.appContext().getString(R.string.degrees))
    }

    override fun toString(): String {
        return "Weather(forecastDay=$forecastDay, forecastTime=$forecastTime,temp=$temp,minTemp=$minTemp,maxTemp=$maxTemp,skyDescription=$skyDescription" +
                "skyCode=$skyCode, rainAmount=$rainAmount,rainPercent=$rainPercent,rainDescription=$rainDescription,snowDescription=$snowDescription,windDirection=$windDirection" +
                "windSpeed=$windSpeed, humidity=$humidity,windDirectionCode=$windDirectionCode,windDirectionDesc=$windDirectionDesc,pm10Amount=$pm10Amount,pm10Description=$pm10Description" +
                "pm25Amount=$pm25Amount, pm25Description=$pm25Description,uvLevel=$uvLevel,uvDescription=$uvDescription,yesterdayTemp=$yesterdayTemp,apparentTemp=$apparentTemp,hour=$hour)"

    }
}