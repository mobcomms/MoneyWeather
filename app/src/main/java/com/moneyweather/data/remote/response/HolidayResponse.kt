package com.moneyweather.data.remote.response

import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.Calendar


data class HolidayResponse(var result: Int, var msg: String, var data: Data) {
    data class Data(
        var list: ArrayList<HolidayItem>
    )

    data class HolidayItem(
        var name: String, var dateKey: Int, var dateString: String
    )
}

fun HolidayResponse.convertToCalendarList(): List<CalendarDay> {
    val holidays = mutableListOf<Calendar>()

    data.list.forEach { holidayItem ->
        val year = holidayItem.dateString.substring(0 until 4).toInt()
        val month = holidayItem.dateString.substring(5 until 7).toInt()
        val day = holidayItem.dateString.substring(8 until 10).toInt()

        holidays.add(Calendar.getInstance().apply {
            set(year, month - 1, day)
        })
    }

    // Calendar → CalendarDay 변환
    return holidays.map {
        CalendarDay.from(it.get(Calendar.YEAR), it.get(Calendar.MONTH) + 1, it.get(Calendar.DAY_OF_MONTH))
    }
}