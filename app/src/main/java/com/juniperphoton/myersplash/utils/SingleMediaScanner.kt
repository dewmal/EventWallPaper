package com.juniperphoton.myersplash.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File

object SingleMediaScanner {
    fun sendScanFileBroadcast(ctx: Context, file: File) {
        var intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.data = Uri.fromFile(file)
        ctx.sendBroadcast(intent)
    }
}