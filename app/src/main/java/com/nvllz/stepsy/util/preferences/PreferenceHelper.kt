package com.nvllz.stepsy.util.preferences

import android.content.Context
import android.content.SharedPreferences
import com.nvllz.stepsy.util.preferences.models.FirstDayOfWeek
import com.nvllz.stepsy.util.preferences.models.ThemeMode
import com.nvllz.stepsy.util.preferences.models.ThemeStyle
import com.nvllz.stepsy.util.preferences.models.UnitSystem
import androidx.core.content.edit
import com.nvllz.stepsy.util.preferences.models.DateFormat

import com.nvllz.stepsy.R

class PreferenceHelper private constructor(val context: Context) {
    private val sharedPreferences: SharedPreferences =
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    val language: String
        get() = sharedPreferences.getString(context.getString(R.string.key_app_language), "system") ?: "system"


    var themeMode: ThemeMode
        get() {
            val value = sharedPreferences.getString(context.getString(ThemeMode.key()), ThemeMode.default().value)
                ?: ThemeMode.default().value
            return ThemeMode.fromValue(value)
        }
        set(value) {
            sharedPreferences.edit { putString(context.getString(ThemeMode.key()), value.value) }
        }

    var themeStyle: ThemeStyle
        get() {
            val value = sharedPreferences.getString(context.getString(ThemeStyle.key()), ThemeStyle.default().value)
                ?: ThemeStyle.default().value
            return ThemeStyle.fromValue(value)
        }
        set(value) {
            sharedPreferences.edit { putString(context.getString(ThemeStyle.key()), value.value) }
        }

    var dateFormat: DateFormat
        get() {
            val value = sharedPreferences.getString(context.getString(DateFormat.key()), DateFormat.default().value)
                ?: DateFormat.default().value
            return DateFormat.fromValue(value)
        }
        set(value) {
            sharedPreferences.edit { putString(context.getString(DateFormat.key()), value.value) }
        }

    var firstDayOfWeek: FirstDayOfWeek
        get() {
            val value = sharedPreferences.getString(context.getString(FirstDayOfWeek.key()), FirstDayOfWeek.default().value.toString())
                ?: FirstDayOfWeek.default().value.toString()
            return FirstDayOfWeek.fromValue(value)
        }
        set(value) {
            sharedPreferences.edit { putString(context.getString(FirstDayOfWeek.key()), value.value.toString()) }
        }

    var height: Int
        get() = sharedPreferences.getString(context.getString(R.string.key_height), "170")?.toInt() ?: 170
        set(value) {
            sharedPreferences.edit { putString(context.getString(R.string.key_height), value.toString()) }
        }

    var weight: Int
        get() = sharedPreferences.getString(context.getString(R.string.key_weight), "70")?.toInt() ?: 70
        set(value) {
            sharedPreferences.edit { putString(context.getString(R.string.key_weight), value.toString()) }
        }

    var unitSystem: UnitSystem
        get() {
            val value = sharedPreferences.getString(context.getString(UnitSystem.key()), UnitSystem.default().value)
                ?: UnitSystem.default().value
            return UnitSystem.fromValue(value)
        }
        set(value) {
            sharedPreferences.edit { putString(context.getString(UnitSystem.key()), value.value) }
        }


    var steps: Int
        get() = sharedPreferences.getInt(KEY_STEPS, 0)
        set(value) {
            sharedPreferences.edit { putInt(KEY_STEPS, value) }
        }

    var stepsDate: Long
        get() = sharedPreferences.getLong(KEY_DATE, 0)
        set(value) {
            sharedPreferences.edit { putLong(KEY_DATE, value) }
        }

    var paused: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_PAUSED, false)
        set(value) {
            sharedPreferences.edit { putBoolean(KEY_IS_PAUSED, value) }
        }


    companion object {
        @Volatile private var INSTANCE: PreferenceHelper? = null

        fun getInstance(context: Context): PreferenceHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferenceHelper(context).also { INSTANCE = it }
            }
        }

        const val KEY_STEPS = "STEPS"
        const val KEY_DATE = "DATE"
        const val KEY_IS_PAUSED = "IS_PAUSED"
    }
}