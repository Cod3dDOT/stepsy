/*
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.nvllz.stepsy.util

import com.nvllz.stepsy.util.preferences.PreferenceHelper
import com.nvllz.stepsy.util.preferences.models.UnitSystem

class StepsHelper(val preferenceHelper: PreferenceHelper) {
    internal fun stepsToDistance(steps: Int): Double {
        val height = preferenceHelper.height
        val unitSystem = preferenceHelper.unitSystem


        val meters = (steps * height * 0.41) / 100000
        return when (unitSystem) {
            UnitSystem.Imperial -> meters * 0.621371
            else -> meters
        }
    }

    internal fun getDistanceUnitString(): String {
        val unitSystem = preferenceHelper.unitSystem

        return when (unitSystem) {
            UnitSystem.Metric -> "km"
            UnitSystem.Imperial -> "mi"
        }
    }

    internal fun stepsToCalories(steps: Int): Int {
        val weight = preferenceHelper.weight

        return (steps * weight * 0.0005).toInt()
    }
}
