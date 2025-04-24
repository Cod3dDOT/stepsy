/*
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.nvllz.stepsy.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceFragmentCompat
import com.nvllz.stepsy.R
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.LocaleListCompat
import com.nvllz.stepsy.util.ThemeHelper
import com.nvllz.stepsy.util.preferences.PreferenceHelper
import com.nvllz.stepsy.util.preferences.models.ThemeMode
import com.nvllz.stepsy.util.preferences.models.ThemeStyle
import com.nvllz.stepsy.util.preferences.models.UnitSystem
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val color = ContextCompat.getColor(this, R.color.colorBackground)
        supportActionBar?.setBackgroundDrawable(color.toDrawable())

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
        private lateinit var preferenceHelper: PreferenceHelper
        private lateinit var themeHelper: ThemeHelper

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            preferenceHelper = PreferenceHelper.getInstance(requireContext())
            themeHelper = ThemeHelper.getInstance(requireContext(), preferenceHelper)
        }

        override fun onResume() {
            super.onResume()
            preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
            when (key) {
                getString(ThemeMode.key()) -> updateTheme()
                getString(ThemeStyle.key()) -> updateTheme()
                getString(UnitSystem.key()) -> updateUnitSystem()
                getString(R.string.key_app_language) -> updateLanguage()
            }
        }

        private fun updateTheme() {
            themeHelper.applyThemeMode()
            themeHelper.applyThemeStyle(this.activity as SettingsActivity)
        }

        private fun updateUnitSystem() {

        }

        private fun updateLanguage () {
            val localeCode = preferenceHelper.language

            val newLocale = when (localeCode) {
                "system" -> LocaleListCompat.getEmptyLocaleList() // Use system's default language
                else -> LocaleListCompat.create(Locale(localeCode)) // Create a new locale from the selected language
            }

            // Apply the new locale
            AppCompatDelegate.setApplicationLocales(newLocale)
        }
    }
}