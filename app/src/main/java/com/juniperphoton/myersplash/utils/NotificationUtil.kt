package com.juniperphoton.myersplash.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.util.SparseArray
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.activity.ManageDownloadActivity
import com.juniperphoton.myersplash.service.BackgroundDownloadService
import java.util.*

class NotificationUtil {
    companion object {
        private var mLastId = 0
        var NOT_ALLOCATED_ID = -10000
        private val uriHashMap = HashMap<Uri, Int>()
        private val integerBuilderHashMap = SparseArray<NotificationCompat.Builder>()

        private fun findNIdByUri(downloadUri: Uri): Int {
            var nId: Int = NOT_ALLOCATED_ID
            if (uriHashMap.containsKey(downloadUri)) {
                nId = uriHashMap[downloadUri] ?: -1
            }
            return nId
        }

        private fun findBuilderById(id: Int): NotificationCompat.Builder? {
            if (integerBuilderHashMap.get(id) != null) {
                return integerBuilderHashMap.get(id)
            }
            return null
        }

        private val notificationManager: NotificationManager
            get() = App.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fun cancelNotification(downloadUri: Uri) {
            val nId = findNIdByUri(downloadUri)
            if (nId != NOT_ALLOCATED_ID) {
                notificationManager.cancel(nId)
            }
        }

        fun cancelNotificationById(nId: Int) {
            if (nId != NOT_ALLOCATED_ID) {
                notificationManager.cancel(nId)
            }
        }

        fun showErrorNotification(downloadUri: Uri, fileName: String, url: String) {
            var nId: Int
            nId = findNIdByUri(downloadUri)
            if (nId == NOT_ALLOCATED_ID) {
                uriHashMap.put(downloadUri, mLastId)
                nId = mLastId
                mLastId++
            }

            val intent = Intent(App.instance, BackgroundDownloadService::class.java)
            intent.putExtra(Params.NAME_KEY, fileName)
            intent.putExtra(Params.URL_KEY, url)
            intent.putExtra(Params.CANCEL_NID_KEY, nId)

            val resultPendingIntent = PendingIntent.getService(App.instance, 0, intent, 0)

            val builder = NotificationCompat.Builder(App.instance)
                    .setContentTitle(App.instance.getString(R.string.download_error))
                    .setContentText(App.instance.getString(R.string.download_error_retry))
                    .setSmallIcon(R.drawable.vector_ic_clear_white)

            builder.addAction(R.drawable.ic_replay_white_48dp, App.instance.getString(R.string.retry_act),
                    resultPendingIntent)

            notificationManager.notify(nId, builder.build())
        }

        fun showCompleteNotification(downloadUri: Uri, fileUri: Uri) {
            val nId: Int = findNIdByUri(downloadUri)

            val builder = NotificationCompat.Builder(App.instance)
                    .setContentTitle(App.instance.getString(R.string.saved))
                    .setContentText(App.instance.getString(R.string.tap_to_open_manage))
                    .setSmallIcon(R.drawable.small_download_ok)

            //File file = new File(fileUri.getPath());
            //Uri uri = FileProvider.getUriForFile(App.instance, App.instance.getString(R.string.authorities), file);
            //Intent intent =  WallpaperManager.getInstance(App.instance).getCropAndSetWallpaperIntent(uri);
            injectIntent(builder)

            if (nId != NOT_ALLOCATED_ID) {
                notificationManager.notify(nId, builder.build())
            }
        }

        fun showProgressNotification(title: String, content: String, progress: Int,
                                     filePath: String, downloadUri: Uri) {
            var nId: Int
            nId = findNIdByUri(downloadUri)
            if (nId == NOT_ALLOCATED_ID) {
                uriHashMap.put(downloadUri, mLastId)
                nId = mLastId
                mLastId++
            }

            var builder = findBuilderById(nId)
            if (builder == null) {
                builder = NotificationCompat.Builder(App.instance)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setSmallIcon(R.drawable.vector_ic_file_download)
                integerBuilderHashMap.put(nId, builder)
            } else {
                builder.setProgress(100, progress, false)
            }
            injectIntent(builder)
            notificationManager.notify(nId, builder!!.build())
        }

        private fun injectIntent(builder: NotificationCompat.Builder?) {
            val intent = Intent(App.instance, ManageDownloadActivity::class.java)
            val stackBuilder = TaskStackBuilder.create(App.instance)
            stackBuilder.addNextIntent(intent)

            val resultPendingIntent = stackBuilder.getPendingIntent(0, FLAG_UPDATE_CURRENT)
            builder?.setContentIntent(resultPendingIntent)
        }
    }

}
