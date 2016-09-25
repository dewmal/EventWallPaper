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

import java.util.HashMap;
import java.util.Hashtable;

public class NotificationUtil {

    private static int mLastId = 0;
    private static HashMap<Uri, Integer> uriHashMap = new HashMap<>();

    private static int findNIdByUri(Uri downloadUri) {
        int nId = -1;
        if (uriHashMap.containsKey(downloadUri)) {
            nId = uriHashMap.get(downloadUri);
        }
        return nId;
    }

    public static void cancelNotification(Uri downloadUri) {
        NotificationManager notificationManager =
                (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        int nId = findNIdByUri(downloadUri);
        if (nId != -1) {

            notificationManager.cancel(nId);
        }
    }

    public static void showErrorNotification(Uri downloadUri) {
        NotificationManager notificationManager =
                (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        int nId = findNIdByUri(downloadUri);
        if (nId != -1) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(App.getInstance())
                    .setContentTitle("Error")
                    .setContentText("Download error.")
                    .setSmallIcon(R.drawable.ic_cancel_white_36dp);

            notificationManager.notify(nId, mBuilder.build());
        }
    }

    public static void sendNotification(String title, String content, boolean completed, Uri downloadUri) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(App.getInstance())
                .setContentTitle(title)
                .setContentText(content)
                .setTicker(content)
                .setAutoCancel(true);
        if (completed) {
            mBuilder.setSmallIcon(R.drawable.small_icon);
        } else {
            mBuilder.setProgress(100, 50, true);
            mBuilder.setSmallIcon(R.drawable.download_small_icon);
        }

        if (completed) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(downloadUri, "image/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getInstance());
            stackBuilder.addNextIntent(intent);

            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
        }

        NotificationManager notificationManager =
                (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

        int nId = -1;
        nId = findNIdByUri(downloadUri);
        if (nId == -1) {
            uriHashMap.put(downloadUri, mLastId);
            nId = mLastId;
            mLastId++;
        }
        notificationManager.notify(nId, mBuilder.build());
    }
}
