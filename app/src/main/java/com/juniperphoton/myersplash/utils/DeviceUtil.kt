package com.juniperphoton.myersplash.utils

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.Display
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.WindowManager

@Suppress("UNUSED")
class DeviceUtil {
    companion object {
        fun hasNavigationBar(ctx: Context): Boolean {
            var hasBackKey = try {
                KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
            } catch (e: Exception) {

            }
            val size = getNavigationBarSize(ctx)
            return size.y > 0
        }

        fun getNavigationBarSize(ctx: Context): Point {
            val appUsableSize = getAppUsableScreenSize(ctx)
            val realScreenSize = getRealScreenSize(ctx)

            // navigation bar on the right
            if (appUsableSize.x < realScreenSize.x) {
                return Point(realScreenSize.x - appUsableSize.x, appUsableSize.y)
            }

            // navigation bar at the bottom
            if (appUsableSize.y < realScreenSize.y) {
                return Point(appUsableSize.x, realScreenSize.y - appUsableSize.y)
            }

            // navigation bar is not present
            return Point()
        }

        fun getAppUsableScreenSize(ctx: Context): Point {
            val display = (ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val size = Point()
            display.getSize(size)
            return size
        }

        fun getRealScreenSize(ctx: Context): Point {
            var windowManager = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            var display = windowManager.defaultDisplay
            var size = Point()
            if (Build.VERSION.SDK_INT >= 17) {
                display.getRealSize(size)
            } else if (Build.VERSION.SDK_INT >= 14) {
                try {
                    size.x = Display::class.java.getMethod("getRawWidth").invoke(display) as Int
                    size.y = Display::class.java.getMethod("getRawHeight").invoke(display) as Int
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            return size
        }
    }
}