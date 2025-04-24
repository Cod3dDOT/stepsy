package com.nvllz.stepsy.util.preferences.models

import com.nvllz.stepsy.R

sealed class FirstDayOfWeek(val value: Int) {
    object Saturday : FirstDayOfWeek(6)
    object Sunday : FirstDayOfWeek(7)
    object Monday : FirstDayOfWeek(1)

    companion object {
        fun fromValue(value: String): FirstDayOfWeek {
            return when(value.toInt()) {
                Saturday.value -> Saturday
                Sunday.value -> Sunday
                else -> Monday
            }
        }

        fun default() = Monday

        fun key() = R.string.key_first_day_of_the_week
    }
}