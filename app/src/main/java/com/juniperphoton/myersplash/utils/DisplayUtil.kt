package com.juniperphoton.myersplash.utils

import android.content.Context

class DisplayUtil {
    companion object {
        fun getDpi(context: Context): Float {
            return context.resources.displayMetrics.density
        }

        fun getDimenInPixel(valueInDP: Int, context: Context): Int {
            return (valueInDP * getDpi(context)).toInt()
        }
    }
}
