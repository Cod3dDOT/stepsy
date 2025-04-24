package com.nvllz.stepsy.ui

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nvllz.stepsy.R
import com.nvllz.stepsy.util.StepsHelper
import com.nvllz.stepsy.util.preferences.PreferenceHelper

class WidgetStepsCompactProvider : AppWidgetProvider() {
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var stepsHelper: StepsHelper

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        preferenceHelper = PreferenceHelper.getInstance(context)
        stepsHelper = StepsHelper(preferenceHelper)
    }

    companion object {
        @SuppressLint("DefaultLocale")
        fun updateWidget(context: Context, steps: Int, distance: Double, distanceUnit: String) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetComponent = ComponentName(context, WidgetStepsCompactProvider::class.java)
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_steps_compact)

            val distanceStr = String.format("%.2f %s", distance, distanceUnit)

            remoteViews.setTextViewText(R.id.widget_steps, steps.toString())
            remoteViews.setTextViewText(R.id.widget_distance, distanceStr)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            remoteViews.setOnClickPendingIntent(R.id.widget_compact_container, pendingIntent)

            appWidgetManager.updateAppWidget(widgetComponent, remoteViews)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val steps = preferenceHelper.steps
        val distance = stepsHelper.stepsToDistance(steps)
        val distanceUnit = stepsHelper.getDistanceUnitString()

        updateWidget(context, steps, distance, distanceUnit)
    }
}
