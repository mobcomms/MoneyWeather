package com.moneyweather.util.calendar

import android.content.Context
import androidx.core.content.ContextCompat
import com.moneyweather.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class SelectedDayDecorator(
    val context: Context,
    private val selectedDate: CalendarDay
) : DayViewDecorator {

    private val today = CalendarDay.today()

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day != today && day == selectedDate
    }

    override fun decorate(view: DayViewFacade) {
        ContextCompat.getDrawable(context, R.drawable.calendar_selected_bg)?.let {
            view.setBackgroundDrawable(it)
        }
    }
}