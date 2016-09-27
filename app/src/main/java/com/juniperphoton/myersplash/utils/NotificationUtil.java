package com.juniperphoton.myersplash.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.FileProvider;
import android.util.SparseArray;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.base.App;
import com.juniperphoton.myersplash.service.BackgroundDownloadService;

import java.io.File;
import java.util.HashMap;

public class NotificationUtil {

    private static int mLastId = 0;
    public static int NOT_ALLOCATED_ID = -10000;
    private static HashMap<Uri, Integer> uriHashMap = new HashMap<>();
    private static SparseArray<NotificationCompat.Builder> integerBuilderHashMap = new SparseArray<>();

    private static int findNIdByUri(Uri downloadUri) {
        int nId = NOT_ALLOCATED_ID;
        if (uriHashMap.containsKey(downloadUri)) {
            nId = uriHashMap.get(downloadUri);
        }
        return nId;
    }

    private static NotificationCompat.Builder findBuilderById(int id) {
        if (integerBuilderHashMap.get(id) != null) {
            return integerBuilderHashMap.get(id);
        }
        return null;
    }

    private static NotificationManager getNotificationManager() {
        return (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void cancelNotification(Uri downloadUri) {
        int nId = findNIdByUri(downloadUri);
        if (nId != NOT_ALLOCATED_ID) {
            getNotificationManager().cancel(nId);
        }
    }

    public static void cancelNotificationById(int nId) {
        if (nId != NOT_ALLOCATED_ID) {
            getNotificationManager().cancel(nId);
        }
    }

    public static void showErrorNotification(Uri downloadUri, String fileName, String url) {
        int nId;
        nId = findNIdByUri(downloadUri);
        if (nId == NOT_ALLOCATED_ID) {
            uriHashMap.put(downloadUri, mLastId);
            nId = mLastId;
            mLastId++;
        }

        Intent intent = new Intent(App.getInstance(), BackgroundDownloadService.class);
        intent.putExtra("NAME", fileName);
        intent.putExtra("URI", url);
        intent.putExtra("NID", nId);

        PendingIntent resultPendingIntent = PendingIntent.getService(App.getInstance(), 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(App.getInstance())
                .setContentTitle("Download error.")
                .setContentText("Please checkAndRequest your network and retry.")
                .setSmallIcon(R.drawable.ic_cancel_white_36dp);

        builder.addAction(R.drawable.ic_replay_white_48dp, "Retry", resultPendingIntent);

        getNotificationManager().notify(nId, builder.build());
    }

    public static void showCompleteNotification(Uri downloadUri, Uri fileUri) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(App.getInstance())
                .setContentTitle("Saved :D")
                .setContentText("Tap to crop and set as wallpaper.")
                .setSmallIcon(R.drawable.small_icon);

        File file = new File(fileUri.getPath());
        Uri uri = FileProvider.getUriForFile(App.getInstance(), App.getInstance().getString(R.string.authorities), file);
        Intent intent = WallpaperManager.getInstance(App.getInstance()).getCropAndSetWallpaperIntent(uri);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getInstance());
        stackBuilder.addNextIntent(intent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        int nId;
        nId = findNIdByUri(downloadUri);
        if (nId != NOT_ALLOCATED_ID) {
            getNotificationManager().notify(nId, mBuilder.build());
        }
    }

    public static void showProgressNotification(String title, String content, int progress, Uri downloadUri) {
        int nId;
        nId = findNIdByUri(downloadUri);
        if (nId == NOT_ALLOCATED_ID) {
            uriHashMap.put(downloadUri, mLastId);
            nId = mLastId;
            mLastId++;
        }

        NotificationCompat.Builder builder = findBuilderById(nId);
        if (builder == null) {
            builder = new NotificationCompat.Builder(App.getInstance())
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(R.drawable.download_small_icon);
            integerBuilderHashMap.put(nId, builder);
        } else {
            builder.setProgress(100, progress, false);
        }
        getNotificationManager().notify(nId, builder.build());
    }
}
