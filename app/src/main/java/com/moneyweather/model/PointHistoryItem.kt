package com.moneyweather.model

import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.moneyweather.R
import com.moneyweather.base.BaseApplication

class PointHistoryItem {
    var description: String? = ""
    var point: Int? = 0
    var historyType: Int? = 0
    var createdAt: String? = ""

    override fun toString(): String {
        return "PointHitoryItem(description=$description, point=$point,historyType=$historyType,createdAt=$createdAt)"
    }

    fun pointText() : SpannableStringBuilder?{
        val context = BaseApplication.appContext()
        var str = ""
        var colorSpan : ForegroundColorSpan
        if(historyType == 2){ //사용
            str = String.format("-%d P",point)
            colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange_color))
        }else{ //적립
            str = String.format("+%d P",point)
            colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.default_main_color))
        }

        val spannableString = SpannableString(str)
        val builder = SpannableStringBuilder(spannableString)

        val begin = 0
        val end = builder.length

        builder.setSpan(
            colorSpan,
            begin,
            end,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        return builder
    }
}