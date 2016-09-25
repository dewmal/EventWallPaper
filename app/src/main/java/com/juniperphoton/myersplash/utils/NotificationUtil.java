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

public class NotificationUtil {
    public static void sendNotification(String title, String content, boolean completed) {
        NotificationUtil.sendNotification(title, content, completed, null);
    }

    public static void sendNotification(String title, String content, boolean completed, Uri fileUri) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(App.getInstance())
                .setContentTitle(title)
                .setContentText(content)
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
            intent.setDataAndType(fileUri, "image/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getInstance());
            stackBuilder.addNextIntent(intent);

            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
        }

        NotificationManager notificationManager =
                (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, mBuilder.build());
    }
}
