package com.juniperphoton.myersplash.utils

import android.app.WallpaperManager
import android.content.Intent
import android.support.v4.content.FileProvider
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.BuildConfig
import java.io.File

object IntentUtil {
    fun getSetAsWallpaperIntent(filePath: String): Intent {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(App.instance, "${BuildConfig.APPLICATION_ID}.fileProvider", file)
        val intent = WallpaperManager.getInstance(App.instance).getCropAndSetWallpaperIntent(uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }
}