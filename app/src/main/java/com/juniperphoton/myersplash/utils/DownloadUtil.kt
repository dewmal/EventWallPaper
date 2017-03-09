package com.juniperphoton.myersplash.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.util.Log
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.event.DownloadStartedEvent
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.service.BackgroundDownloadService
import io.realm.Realm
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import java.io.*
import java.lang.Exception
import java.util.*

object DownloadUtil {
    private const val TAG = "DownloadUtil"

    fun writeResponseBodyToDisk(body: ResponseBody,
                                fileUri: String,
                                downloadUrl: String): File? {
        try {
            var fileToSave = File(fileUri)
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                var startTime = Date().time
                inputStream = body.byteStream()
                outputStream = FileOutputStream(fileToSave)

                val buffer = ByteArray(4096)
                var fileSize = body.contentLength()
                var downloadedSize: Long = 0
                var progressToReport = 0

                while (true) {
                    var read = inputStream.read(buffer)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(buffer, 0, read)
                    downloadedSize += read

                    var progress = (downloadedSize / fileSize.toDouble() * 100).toInt()
                    if (progress - progressToReport >= 5) {
                        progressToReport = progress
                        NotificationUtil.showProgressNotification("MyerSplash", "Downloading...",
                                progressToReport, fileUri, Uri.parse(downloadUrl))
                        Realm.getDefaultInstance().executeTransaction { realm ->
                            val downloadItem = realm.where(DownloadItem::class.java)
                                    .equalTo(DownloadItem.DOWNLOAD_URL, downloadUrl).findFirst()
                            if (downloadItem != null) {
                                downloadItem.progress = progressToReport
                            }
                        }
                    }

                    val endTime = Date().time

                    Log.d(TAG, "time spend=" + (endTime - startTime).toString())

                    outputStream.flush()

                    return fileToSave
                }
            } catch (e: Exception) {
                return null
            } finally {
                try {
                    inputStream?.close()
                    outputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ToastService.sendShortToast(e.message)
            return null
        }
    }

    fun getFileToSave(expectedName: String): File? {
        val galleryPath = getGalleryPath() ?: return null
        val folder = File(getGalleryPath()!!)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return File(folder.absolutePath + File.separator + expectedName)
    }

    fun getGalleryPath(): String? {
        val mediaStorageDir: File
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) ?: return ""
            mediaStorageDir = File(path, "MyerSplash")
        } else {
            val extStorageDirectory = App.instance.filesDir.absolutePath
            mediaStorageDir = File(extStorageDirectory, "MyerSplash")
        }

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }

        return mediaStorageDir.absolutePath
    }

    fun copyFile(srcF: File, destF: File): Boolean {
        if (!destF.exists()) {
            try {
                if (!destF.createNewFile()) {
                    return false
                }
            } catch (e: Exception) {
                return false
            }

        }

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            try {
                inputStream = FileInputStream(srcF)
                outputStream = FileOutputStream(destF)

                val fileReader = ByteArray(4096)
                while (true) {
                    val read = inputStream.read(fileReader)

                    if (read == -1) {
                        break
                    }

                    outputStream.write(fileReader, 0, read)
                }
                outputStream.flush()
            } catch (e: Exception) {
                e.printStackTrace()
                return false
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
            e.printStackTrace()
            return false
        }
        return true
    }

    fun cancelDownload(context: Context, image: UnsplashImage) {
        val intent = Intent(App.instance, BackgroundDownloadService::class.java)
        intent.putExtra(Params.CANCELED_KEY, true)
        intent.putExtra(Params.URL_KEY, image.downloadUrl)
        context.startService(intent)
    }

    fun checkAndDownload(context: Activity, image: UnsplashImage) {
        if (!PermissionUtil.check(context)) {
            ToastService.sendShortToast(context.getString(R.string.no_permission))
            return
        }
        if (!NetworkUtil.usingWifi(context)) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.attention)
            builder.setMessage(R.string.wifi_attention_content)
            builder.setPositiveButton(R.string.download) { dialog, which ->
                dialog.dismiss()
                startDownloadService(context, image)
                EventBus.getDefault().post(DownloadStartedEvent(image.id))
            }
            builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
            builder.create().show()
        } else {
            startDownloadService(context, image)
            EventBus.getDefault().post(DownloadStartedEvent(image.id))
        }
    }

    fun getDownloadItemById(id: String): DownloadItem {
        val realm = Realm.getDefaultInstance()
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

        ToastService.sendShortToast(context.getString(R.string.downloading_in_background))

        val item = DownloadItem(image.id, image.listUrl, image.downloadUrl,
                image.fileNameForDownload)
        item.color = image.themeColor
        Realm.getDefaultInstance().executeTransaction { realm -> realm.copyToRealmOrUpdate(item) }
    }
}