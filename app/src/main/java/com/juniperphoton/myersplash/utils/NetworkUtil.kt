package com.juniperphoton.myersplash.utils

import android.content.Context
import android.net.ConnectivityManager

object NetworkUtil {
    fun usingWifi(ctx: Context): Boolean {
        var manager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var info = manager.activeNetworkInfo
        return info?.type == ConnectivityManager.TYPE_WIFI
    }
}