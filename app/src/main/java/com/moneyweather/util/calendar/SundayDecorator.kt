package com.moneyweather.util.calendar

import android.content.Context
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.moneyweather.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.Calendar

class SundayDecorator(
    val context: Context,
    private val selectedYear: Int,
    private val selectedMonth: Int
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        val calendar = Calendar.getInstance().apply {
            set(day.year, day.month - 1, day.day)
        }

        val isSelectedMonth = day.year == selectedYear && day.month == selectedMonth
        val isSunday = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY

        return isSelectedMonth && isSunday
    }

    override fun decorate(view: DayViewFacade) {
        val color = ContextCompat.getColor(context, R.color.calendar_red)

        view.addSpan(ForegroundColorSpan(color))
    }
}