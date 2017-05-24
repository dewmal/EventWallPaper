package com.juniperphoton.myersplash.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View

object StatusBarCompat {
    fun setUpActivity(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 19 && !isChrome()) {
            activity.window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        if (Build.VERSION.SDK_INT >= 21) {
            activity.window.statusBarColor = Color.TRANSPARENT
        }
    }

    fun isChrome(): Boolean {
        return Build.BRAND === "chromium" || Build.BRAND === "chrome"
    }

    fun setDarkText(activity: Activity, darkText: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var systemUiVisibility = activity.window.decorView.systemUiVisibility
            if (darkText) {
                systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            activity.window.decorView.systemUiVisibility = systemUiVisibility
        }
    }
}
