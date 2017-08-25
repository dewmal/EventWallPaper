package com.juniperphoton.myersplash.extension

import android.graphics.Color

fun Int.isLightColor(): Boolean = getLuma() > 120

fun Int.getLuma(): Double =
        0.299 * Color.red(this) + 0.587 * Color.green(this) + 0.114 * Color.blue(this)

fun Int.getDarker(alpha: Float): Int =
        Color.rgb((Color.red(this) * alpha).toInt(), (Color.green(this) * alpha).toInt(),
                (Color.blue(this) * alpha).toInt())