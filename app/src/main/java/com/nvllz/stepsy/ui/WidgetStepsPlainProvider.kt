package com.nvllz.stepsy.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nvllz.stepsy.R
import com.nvllz.stepsy.util.preferences.PreferenceHelper

class WidgetStepsPlainProvider : AppWidgetProvider() {
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        preferenceHelper = PreferenceHelper.getInstance(context)
    }

    companion object {
        fun updateWidget(context: Context, steps: Int) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetComponent = ComponentName(context, WidgetStepsPlainProvider::class.java)
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_steps_plain)
            val resources = context.resources

            val stepsStr = resources.getQuantityString(
                R.plurals.steps_text,
                steps,
                steps
            )

            remoteViews.setTextViewText(R.id.widget_steps, stepsStr)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            remoteViews.setOnClickPendingIntent(R.id.widget_plain_container, pendingIntent)

            appWidgetManager.updateAppWidget(widgetComponent, remoteViews)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val steps = PreferenceHelper.getInstance(context).steps

        updateWidget(context, steps)
    }
}
