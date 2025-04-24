package com.nvllz.stepsy.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.nvllz.stepsy.util.preferences.PreferenceHelper
import com.nvllz.stepsy.util.preferences.models.ThemeMode
import com.nvllz.stepsy.util.preferences.models.ThemeStyle
import com.nvllz.stepsy.R

class ThemeHelper private constructor(private val context: Context, private val preferenceHelper: PreferenceHelper) {
    /**
     * Apply the light/dark/system theme mode
     */
    fun applyThemeMode() {
        when (preferenceHelper.themeMode) {
            is ThemeMode.Light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            is ThemeMode.Dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            is ThemeMode.System -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    /**
     * Apply the theme style to an activity (needs to be called in onCreate before setContentView)
     */
    fun applyThemeStyle(activity: Activity) {
        val isDarkMode = when (preferenceHelper.themeMode) {
            is ThemeMode.Light -> false
            is ThemeMode.Dark -> true
            is ThemeMode.System -> isDarkModeActive()
        }

        val themeStyle = preferenceHelper.themeStyle

        // Set the theme based on current settings
        when {
            // Dynamic themes (Android 12+)
            themeStyle is ThemeStyle.Dynamic && isDarkMode -> activity.setTheme(R.style.Theme_DynamicDark)
            themeStyle is ThemeStyle.Dynamic && !isDarkMode -> activity.setTheme(R.style.Theme_DynamicLight)

            // AMOLED themes
            themeStyle is ThemeStyle.Amoled && isDarkMode -> activity.setTheme(R.style.Theme_AmoledDark)
            themeStyle is ThemeStyle.Amoled && !isDarkMode -> activity.setTheme(R.style.Theme_AmoledLight)

            // Default themes
            isDarkMode -> activity.setTheme(R.style.Theme_BaseDark)
            else -> activity.setTheme(R.style.Theme_BaseLight)
        }
    }

    /**
     * Check if dark mode is currently active based on system settings
     */
    private fun isDarkModeActive(): Boolean {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> {
                // Check system night mode
                context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                        Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

    companion object {
        @Volatile private var INSTANCE: ThemeHelper? = null

        fun getInstance(context: Context, preferenceHelper: PreferenceHelper): ThemeHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemeHelper(context, preferenceHelper).also { INSTANCE = it }
            }
        }
    }
}