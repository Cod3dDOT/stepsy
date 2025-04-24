package com.nvllz.stepsy.util

import com.nvllz.stepsy.util.preferences.PreferenceHelper
import java.util.Calendar

class CalendarHelper(preferenceHelper: PreferenceHelper) {
    private val calendar: Calendar = Calendar.getInstance()

    init {
        calendar.firstDayOfWeek = preferenceHelper.firstDayOfWeek.value
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    fun getTime(): Long {
        return calendar.timeInMillis
    }

    fun getFirstDayOfWeek(): Int {
        return calendar.firstDayOfWeek
    }

    fun getWeek(): Int {
        return calendar.get(Calendar.WEEK_OF_YEAR)
    }

    fun set(key: Int, value: Int) {
        calendar.set(key, value)
    }
}