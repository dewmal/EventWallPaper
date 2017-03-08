package com.juniperphoton.myersplash.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

public class SingleMediaScanner {
    public static void sendScanFileBroadcast(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        context.sendBroadcast(intent);
    }
}