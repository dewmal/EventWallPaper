package com.juniperphoton.myersplash.provider

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.RemoteViews
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.activity.MainActivity
import com.juniperphoton.myersplash.cloudservice.CloudService
import com.juniperphoton.myersplash.extension.getLengthInKb
import com.juniperphoton.myersplash.service.DownloadService
import com.juniperphoton.myersplash.utils.AppWidgetUtil
import com.juniperphoton.myersplash.utils.DownloadUtil
import com.juniperphoton.myersplash.utils.FileUtil
import com.juniperphoton.myersplash.utils.Params
import okhttp3.ResponseBody
import rx.Subscriber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class WallpaperWidgetProvider : AppWidgetProvider() {
    companion object {
        private val TAG = "WallpaperWidgetProvider"
    }

    private var thumbUrl: String? = null
        get() {
            return "${CloudService.AUTO_CHANGE_WALLPAPER_THUMB}$dateString.jpg"
        }

    private var downloadUrl: String? = null
        get() {
            return "${CloudService.AUTO_CHANGE_WALLPAPER}$dateString.jpg"
        }

    private var dateString: String? = null
        get() {
            val date = Calendar.getInstance(TimeZone.getDefault())
            return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date.time)
        }

    private var dateStringDisplay: String? = null
        get() {
            val date = Calendar.getInstance(TimeZone.getDefault())
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.time)
        }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        if (appWidgetIds == null || context == null) {
            return
        }
        Log.d(TAG, "onUpdate")

        val file = File(FileUtil.cachedPath, "${thumbUrl!!.hashCode()}.jpg")
        if (file.exists() && file.getLengthInKb() > 100) {
            AppWidgetUtil.doWithWidgetId {
                updateWidget(App.instance, it, file.absolutePath)
            }
            return
        }

        CloudService.downloadPhoto(object : Subscriber<ResponseBody>() {
            var outputFile: File? = null

            override fun onError(e: Throwable?) {
                e!!.printStackTrace()
            }

            override fun onCompleted() {
                outputFile?.let {
                    AppWidgetUtil.doWithWidgetId {
                        updateWidget(App.instance, it, outputFile!!.absolutePath)
                    }
                }
            }

            override fun onNext(responseBody: ResponseBody?) {
                outputFile = DownloadUtil.writeResponseBodyToDisk(responseBody!!, file.path, thumbUrl!!, null)
            }
        }, thumbUrl!!)
    }

    private fun updateWidget(context: Context, widgetId: Int, filePath: String) {
        val manager = AppWidgetManager.getInstance(context)
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)
        remoteViews.setTextViewText(R.id.widget_update_time_text, "$dateStringDisplay Updated")
        val bm = BitmapFactory.decodeFile(filePath)
        remoteViews.setImageViewBitmap(R.id.widget_center_image, bm)

        Log.d(TAG, "pending to download: $downloadUrl")

        val intent = Intent(context, DownloadService::class.java)
        intent.putExtra(Params.URL_KEY, downloadUrl)
        intent.putExtra(Params.NAME_KEY, "$dateString")
        intent.putExtra(Params.PREVIEW_URI, filePath)
        intent.putExtra(Params.IS_UNSPLASH_WALLPAPER, false)
        val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.widget_download_btn, pendingIntent)

        val mainIntent = Intent(context, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.widget_center_image, mainPendingIntent)

        manager.updateAppWidget(widgetId, remoteViews)
    }
}