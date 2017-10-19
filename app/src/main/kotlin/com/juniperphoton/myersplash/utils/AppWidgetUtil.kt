package com.juniperphoton.myersplash.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.broadcastreceiver.WallpaperWidgetProvider

object AppWidgetUtil {
    fun doWithWidgetId(block: ((Int) -> Unit)) {
        val ids = AppWidgetManager.getInstance(App.instance)
                .getAppWidgetIds(ComponentName(App.instance, WallpaperWidgetProvider::class.java))
        ids.forEach {
            block.invoke(it)
        }
    }
}