package com.nvllz.stepsy.util.preferences.models

import com.nvllz.stepsy.R

sealed class ThemeStyle(val value: String) {
    object App : ThemeStyle("default")
    object Amoled : ThemeStyle("amoled")
    object Dynamic : ThemeStyle("dynamic")

    companion object {
        fun fromValue(value: String): ThemeStyle {
            return when(value) {
                Amoled.value -> Amoled
                Dynamic.value -> Dynamic
                else -> App
            }
        }

        fun default() = App

        fun key() = R.string.key_theme_style
    }
}