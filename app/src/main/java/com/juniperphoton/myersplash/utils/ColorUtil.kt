package com.juniperphoton.myersplash.utils

import android.graphics.Color

class ColorUtil {
    companion object {
        fun isColorLight(color: Int): Boolean {
            return getLumaFromColor(color) > 120
        }

        private fun getLumaFromColor(color: Int): Double {
            return 0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)
        }

        fun getDarkerColor(color: Int, alpha: Float): Int {
            return Color.rgb((Color.red(color) * alpha).toInt(), (Color.green(color) * alpha).toInt(),
                    (Color.blue(color) * alpha).toInt())
        }
    }
}