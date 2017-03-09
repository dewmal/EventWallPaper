package com.juniperphoton.myersplash.utils

import android.content.Context

object PackageUtil {
    fun getVersionCode(ctx: Context): Int {
        return try {
            var manager = ctx.packageManager
            var info = manager.getPackageInfo(ctx.packageName, 0)
            return info.versionCode
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
    }

    fun getVersionName(ctx: Context): String? {
        return try {
            var manager = ctx.packageManager
            var info = manager.getPackageInfo(ctx.packageName, 0)
            return info.versionName
        } catch (e: Exception) {
            return null
        }
    }
}