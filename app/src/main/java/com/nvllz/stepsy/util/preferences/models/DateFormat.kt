package com.nvllz.stepsy.util.preferences.models

import com.nvllz.stepsy.R

sealed class DateFormat(val value: String) {
    object ddMMyyyy : DateFormat("DD.MM.YYYY")
    object MMddyyyy : DateFormat("MM.DD.YYYY")
    object yyyyMMdd : DateFormat("YYYY.MM.DD")

    companion object {
        fun fromValue(value: String): DateFormat {
            return when(value) {
                yyyyMMdd.value -> yyyyMMdd
                MMddyyyy.value -> MMddyyyy
                else -> ddMMyyyy
            }
        }

        fun default() = ddMMyyyy

        fun key() = R.string.key_date_format
    }
}