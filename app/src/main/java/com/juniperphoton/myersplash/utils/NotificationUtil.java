package com.juniperphoton.myersplash.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.activity.MainActivity;
import com.juniperphoton.myersplash.base.App;

import java.security.spec.DSAPrivateKeySpec;
import java.util.HashMap;
import java.util.Hashtable;

public class NotificationUtil {

    private static int mLastId = 0;
    private static int NOT_ALLOCATED_ID = -10000;
    private static HashMap<Uri, Integer> uriHashMap = new HashMap<>();
    private static HashMap<Integer, NotificationCompat.Builder> integerBuilderHashMap = new HashMap<>();

    private static int findNIdByUri(Uri downloadUri) {
        int nId = NOT_ALLOCATED_ID;
        if (uriHashMap.containsKey(downloadUri)) {
            nId = uriHashMap.get(downloadUri);
        }
        return nId;
    }

    private static NotificationCompat.Builder findBuilderById(int id) {
        if (integerBuilderHashMap.containsKey(id)) {
            return integerBuilderHashMap.get(id);
        }
        return null;
    }

    private static NotificationManager getNotificationManager() {
        NotificationManager notificationManager =
                (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager;
    }

    public static void cancelNotification(Uri downloadUri) {
        int nId = findNIdByUri(downloadUri);
        if (nId != NOT_ALLOCATED_ID) {
            getNotificationManager().cancel(nId);
        }
    }

    public static void showErrorNotification(Uri downloadUri) {
        int nId = findNIdByUri(downloadUri);
        if (nId != -1) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(App.getInstance())
                    .setContentTitle("Error")
                    .setContentText("Download error.")
                    .setSmallIcon(R.drawable.ic_cancel_white_36dp);
            getNotificationManager().notify(nId, mBuilder.build());
        }
    }

    public static void showCompleteNotification(Uri downloadUri) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(App.getInstance())
                .setContentTitle("MyerSplash")
                .setContentText("Saved :D")
                .setSmallIcon(R.drawable.download_small_icon)
                .setAutoCancel(true);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(downloadUri, "image/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

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
