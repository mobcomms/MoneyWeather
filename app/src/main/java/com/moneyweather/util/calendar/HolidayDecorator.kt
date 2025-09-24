package com.moneyweather.util.calendar

import android.content.Context
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.moneyweather.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class HolidayDecorator(
    val context: Context,
    private val selectedYear: Int,
    private val selectedMonth: Int,
    private val holidays: List<CalendarDay>
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        val isSelectedMonth = day.year == selectedYear && day.month == selectedMonth
        val isHoliday = holidays.contains(day)

        return isSelectedMonth && isHoliday
    }

    override fun decorate(view: DayViewFacade) {
        val color = ContextCompat.getColor(context, R.color.calendar_red)

        view.addSpan(ForegroundColorSpan(color))
    }
}