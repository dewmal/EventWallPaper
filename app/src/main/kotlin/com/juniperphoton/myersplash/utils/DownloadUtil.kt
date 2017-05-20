package com.juniperphoton.myersplash.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.util.Log
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.RealmCache
import com.juniperphoton.myersplash.event.DownloadStartedEvent
import com.juniperphoton.myersplash.extension.usingWifi
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.service.BackgroundDownloadService
import io.realm.Sort
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import java.io.*
import java.lang.Exception
import java.util.*

@Suppress("UNUSED_PARAMETER")
object DownloadUtil {
    private val TAG = "DownloadUtil"

    fun writeResponseBodyToDisk(body: ResponseBody, fileUri: String, downloadUrl: String): File? {
        try {
            val fileToSave = File(fileUri)

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val startTime = Date().time

                inputStream = body.byteStream()
                outputStream = FileOutputStream(fileToSave)

                val buffer = ByteArray(4096)

                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0

                var progressToReport = 0

                while (true) {
                    val read = inputStream!!.read(buffer)

                    if (read == -1) {
                        break
                    }

                    outputStream.write(buffer, 0, read)

                    fileSizeDownloaded += read.toLong()

                    val progress = (fileSizeDownloaded / fileSize.toDouble() * 100).toInt()
                    if (progress - progressToReport >= 5) {
                        progressToReport = progress
                        val progressToDisplay = progressToReport
                        NotificationUtil.showProgressNotification("MyerSplash", "Downloading...",
                                progressToReport, Uri.parse(downloadUrl))
                        RealmCache.getInstance().executeTransaction { realm ->
                            val downloadItem = realm.where(DownloadItem::class.java)
                                    .equalTo(DownloadItem.DOWNLOAD_URL, downloadUrl).findFirst()
                            if (downloadItem != null) {
                                downloadItem.progress = progressToDisplay
                            }
                        }
                    }
                }
                val endTime = Date().time

                Log.d(TAG, "time spend=" + (endTime - startTime).toString())

                outputStream.flush()

                return fileToSave
            } catch (e0: InterruptedIOException) {
                return null
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close()
                    }

                    if (outputStream != null) {
                        outputStream.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        } catch (e: Exception) {
            ToastService.sendShortToast(e.message)
            return null
        }
    }

    fun getFileToSave(expectedName: String): File? {
        val galleryPath = FileUtil.galleryPath ?: return null
        val folder = File(galleryPath)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return File(folder.toString() + File.separator + expectedName)
    }

    fun cancelDownload(context: Context, image: UnsplashImage) {
        val intent = Intent(App.instance, BackgroundDownloadService::class.java)
        intent.putExtra(Params.CANCELED_KEY, true)
        intent.putExtra(Params.URL_KEY, image.downloadUrl)
        context.startService(intent)
    }

    fun checkAndDownload(context: Activity, image: UnsplashImage) {
        if (!context.usingWifi()) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.attention)
            builder.setMessage(R.string.wifi_attention_content)
            builder.setPositiveButton(R.string.download) { dialog, _ ->
                dialog.dismiss()
                startDownloadService(context, image)
                EventBus.getDefault().post(DownloadStartedEvent(image.id))
            }
            builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            builder.create().show()
        } else {
            startDownloadService(context, image)
            EventBus.getDefault().post(DownloadStartedEvent(image.id))
        }
    }

    fun getDownloadItemById(id: String?): DownloadItem? {
        val realm = RealmCache.getInstance()
        realm.beginTransaction()
        val item = realm.where(DownloadItem::class.java)
                .equalTo(DownloadItem.ID_KEY, id)
                .findFirst()
        realm.commitTransaction()
        return item
    }

    private fun startDownloadService(context: Activity, image: UnsplashImage) {
        val intent = Intent(context, BackgroundDownloadService::class.java)
        intent.putExtra(Params.NAME_KEY, image.fileNameForDownload)
        intent.putExtra(Params.URL_KEY, image.downloadUrl)
        context.startService(intent)

        val downloadItems = RealmCache.getInstance().where(DownloadItem::class.java)
                .findAllSorted(DownloadItem.POSITION_KEY, Sort.DESCENDING)
        var position = 0
        if (downloadItems.size > 0) {
            position = downloadItems[0].position + 1
        }

        ToastService.sendShortToast(context.getString(R.string.downloading_in_background))

        val item = DownloadItem(image.id!!, image.listUrl!!, image.downloadUrl!!,
                image.fileNameForDownload)
        item.position = position
        item.color = image.themeColor
        RealmCache.getInstance().executeTransaction { realm -> realm.copyToRealmOrUpdate(item) }
    }
}
