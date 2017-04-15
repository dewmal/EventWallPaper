package com.juniperphoton.myersplash.service

import android.app.IntentService
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.RealmCache
import com.juniperphoton.myersplash.cloudservice.CloudService
import com.juniperphoton.myersplash.extension.sendScanBroadcast
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.utils.DownloadUtil
import com.juniperphoton.myersplash.utils.NotificationUtil
import com.juniperphoton.myersplash.utils.Params
import com.juniperphoton.myersplash.utils.ToastService
import okhttp3.ResponseBody
import rx.Subscriber
import java.io.File
import java.util.*

class BackgroundDownloadService : IntentService("BackgroundDownloadService") {

    override fun onHandleIntent(intent: Intent?) {
        val url = intent!!.getStringExtra(Params.URL_KEY)
        val fileName = intent.getStringExtra(Params.NAME_KEY)
        val canceled = intent.getBooleanExtra(Params.CANCELED_KEY, false)

        val nId = intent.getIntExtra(Params.CANCEL_NID_KEY, NotificationUtil.NOT_ALLOCATED_ID)
        if (nId != NotificationUtil.NOT_ALLOCATED_ID) {
            NotificationUtil.cancelNotificationById(nId)
        }

        if (canceled) {
            Log.d(TAG, "on handle intent cancelled")
            val subscriber = subscriptionMap[url]
            if (subscriber != null) {
                subscriber.unsubscribe()
                NotificationUtil.cancelNotification(Uri.parse(url))
                ToastService.sendShortToast(getString(R.string.cancelled_download))
            }
        } else {
            Log.d(TAG, "on handle intent progress")
            val filePath = downloadImage(url, fileName)
            NotificationUtil.showProgressNotification(getString(R.string.app_name),
                    getString(R.string.downloading),
                    0, filePath, Uri.parse(url))
        }
    }

    fun downloadImage(url: String, fileName: String): String {
        val file = DownloadUtil.getFileToSave(fileName)
        val subscriber = object : Subscriber<ResponseBody>() {
            internal var outputFile: File? = null

            override fun onCompleted() {
                if (outputFile == null) {
                    NotificationUtil.showErrorNotification(Uri.parse(url), fileName, url)
                    RealmCache.getInstance().executeTransaction { realm ->
                        val downloadItem = realm.where(DownloadItem::class.java)
                                .equalTo(DownloadItem.DOWNLOAD_URL, url).findFirst()
                        if (downloadItem != null) {
                            downloadItem.status = DownloadItem.DOWNLOAD_STATUS_FAILED
                        }
                    }
                } else {
                    val realm = RealmCache.getInstance()
                    realm.beginTransaction()

                    val downloadItem = realm.where(DownloadItem::class.java)
                            .equalTo(DownloadItem.DOWNLOAD_URL, url).findFirst()
                    if (downloadItem != null) {
                        downloadItem.status = DownloadItem.DOWNLOAD_STATUS_OK
                        Log.d(TAG, "output file:" + outputFile!!.absolutePath)
                        val newFile = File(outputFile!!.path + ".jpg")
                        outputFile!!.renameTo(newFile)
                        Log.d(TAG, "renamed file:" + newFile.absolutePath)
                        downloadItem.filePath = newFile.path

                        newFile.sendScanBroadcast(App.instance)
                    }

                    realm.commitTransaction()

                    NotificationUtil.showCompleteNotification(Uri.parse(url), Uri.fromFile(outputFile))
                }
                Log.d(TAG, getString(R.string.completed))
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
                Log.d(TAG, "on handle intent error " + e.message + ",url:" + url)
                NotificationUtil.showErrorNotification(Uri.parse(url), fileName, url)

                val realm = RealmCache.getInstance()
                realm.beginTransaction()

                val downloadItem = realm.where(DownloadItem::class.java)
                        .equalTo(DownloadItem.DOWNLOAD_URL, url).findFirst()
                if (downloadItem != null) {
                    downloadItem.status = DownloadItem.DOWNLOAD_STATUS_FAILED
                }

                realm.commitTransaction()
            }

            override fun onNext(responseBody: ResponseBody) {
                Log.d(TAG, "outputFile download onNext,size" + responseBody.contentLength())
                this.outputFile = DownloadUtil.writeResponseBodyToDisk(responseBody, file!!.path, url)
            }
        }
        CloudService.downloadPhoto(subscriber, url)
        subscriptionMap.put(url, subscriber)

        return file!!.path
    }

    companion object {
        private val TAG = BackgroundDownloadService::class.java.name
        private val subscriptionMap = HashMap<String, Subscriber<*>>()
    }
}
