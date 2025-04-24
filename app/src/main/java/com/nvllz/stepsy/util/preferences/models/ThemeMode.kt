package com.nvllz.stepsy.util.preferences.models

import com.nvllz.stepsy.R

sealed class ThemeMode(val value: String) {
    object Light : ThemeMode("light")
    object Dark : ThemeMode("dark")
    object System : ThemeMode("system")

    companion object {
        fun fromValue(value: String): ThemeMode {
            return when(value) {
                Light.value -> Light
                Dark.value -> Dark
                else -> System
            }
        }

        fun default() = System

        fun key() = R.string.key_theme_mode
    }
}