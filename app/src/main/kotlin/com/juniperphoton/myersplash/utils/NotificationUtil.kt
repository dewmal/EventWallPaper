package com.juniperphoton.myersplash.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.NotificationCompat
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.activity.ManageDownloadActivity
import com.juniperphoton.myersplash.service.BackgroundDownloadService

object NotificationUtil {
    private val notificationManager: NotificationManager
        get() = App.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun cancelNotification(downloadUri: Uri) {
        notificationManager.cancel(downloadUri.hashCode())
    }

    fun showErrorNotification(downloadUri: Uri, fileName: String, url: String) {
        val id = downloadUri.hashCode()

        val intent = Intent(App.instance, BackgroundDownloadService::class.java)
        intent.putExtra(Params.NAME_KEY, fileName)
        intent.putExtra(Params.URL_KEY, url)

        val resultPendingIntent = PendingIntent.getService(App.instance, 0, intent, 0)

        val builder = NotificationCompat.Builder(App.instance)
                .setContentTitle(App.instance.getString(R.string.download_error))
                .setContentText(App.instance.getString(R.string.download_error_retry))
                .setSmallIcon(R.drawable.vector_ic_clear_white)

        builder.addAction(R.drawable.ic_replay_white_48dp, App.instance.getString(R.string.retry_act),
                resultPendingIntent)

        notificationManager.notify(id, builder.build())
    }

    fun showCompleteNotification(downloadUri: Uri) {
        val id: Int = downloadUri.hashCode()

        val builder = NotificationCompat.Builder(App.instance)
                .setContentTitle(App.instance.getString(R.string.saved))
                .setContentText(App.instance.getString(R.string.tap_to_open_manage))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.small_download_ok)

        injectIntent(builder)

        notificationManager.notify(id, builder.build())
    }

    fun showProgressNotification(title: String, content: String, progress: Int, downloadUri: Uri) {
        val id = downloadUri.hashCode()

        val builder = NotificationCompat.Builder(App.instance)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.vector_ic_file_download)
                .setProgress(100, progress, false)
        injectIntent(builder)
        notificationManager.notify(id, builder!!.build())
    }

    private fun injectIntent(builder: NotificationCompat.Builder) {
        val intent = Intent(App.instance, ManageDownloadActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivity(App.instance, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(resultPendingIntent)
    }
}
