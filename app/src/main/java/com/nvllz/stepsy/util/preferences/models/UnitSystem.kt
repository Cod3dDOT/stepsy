package com.nvllz.stepsy.util.preferences.models

import com.nvllz.stepsy.R

sealed class UnitSystem(val value: String) {
    object Metric : UnitSystem("metric")
    object Imperial : UnitSystem("imperial")

    companion object {
        fun fromValue(value: String): UnitSystem {
            return when(value) {
                Imperial.value -> Imperial
                else -> Metric
            }
        }

        fun default() = Metric

        fun key() = R.string.key_unit_system
    }
}