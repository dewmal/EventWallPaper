package com.juniperphoton.myersplash.utils

import android.app.Activity
import android.content.ContextWrapper
import android.view.View

object ContextUtil {
    fun getActivity(view: View): Activity? {
        var ctx = view.context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                return ctx
            }
            ctx = (ctx).baseContext
        }
        return null
    }
}