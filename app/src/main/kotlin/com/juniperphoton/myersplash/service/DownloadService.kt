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
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import okhttp3.ResponseBody
import java.io.File
import java.util.*

class DownloadService : IntentService(TAG) {
    companion object {
        private const val TAG = "DownloadService"
        private val disposableMap = HashMap<String, Disposable>()
    }

    private var isUnsplash = true
    private var previewUri: Uri? = null

    override fun onHandleIntent(intent: Intent?) {
        val downloadUrl = intent!!.getStringExtra(Params.URL_KEY)
        val fileName = intent.getStringExtra(Params.NAME_KEY)
        val canceled = intent.getBooleanExtra(Params.CANCELED_KEY, false)
        val previewUrl = intent.getStringExtra(Params.PREVIEW_URI)
        isUnsplash = intent.getBooleanExtra(Params.IS_UNSPLASH_WALLPAPER, true)

        if (!isUnsplash) {
            ToastService.sendShortToast("Downloading...")
        }

        previewUrl?.let {
            previewUri = Uri.parse(previewUrl)
        }

        if (canceled) {
            Log.d(TAG, "on handle intent cancelled")
            val subscriber = disposableMap[downloadUrl]
            if (subscriber != null) {
                subscriber.dispose()
                NotificationUtil.cancelNotification(Uri.parse(downloadUrl))
                ToastService.sendShortToast(getString(R.string.cancelled_download))
            }
        } else {
            Log.d(TAG, "on handle intent progress")
            downloadImage(downloadUrl, fileName)
            NotificationUtil.showProgressNotification(getString(R.string.app_name),
                    getString(R.string.downloading), 0, Uri.parse(downloadUrl), previewUri)
        }
    }

    private fun downloadImage(url: String, fileName: String): String {
        val file = DownloadUtil.getFileToSave(fileName)
        val observer = object : DisposableObserver<ResponseBody>() {
            internal var outputFile: File? = null

            override fun onComplete() {
                if (outputFile == null) {
                    NotificationUtil.showErrorNotification(Uri.parse(url), fileName, url, previewUri)
                    RealmCache.getInstance().executeTransaction { realm ->
                        val downloadItem = realm.where(DownloadItem::class.java)
                                .equalTo(DownloadItem.DOWNLOAD_URL, url).findFirst()
                        if (downloadItem != null) {
                            downloadItem.status = DownloadItem.DOWNLOAD_STATUS_FAILED
                        }
                    }
                } else {
                    Log.d(TAG, "output file:" + outputFile!!.absolutePath)

                    val newFile = File("${outputFile!!.path}.jpg")
                    outputFile!!.renameTo(newFile)

                    Log.d(TAG, "renamed file:" + newFile.absolutePath)
                    newFile.sendScanBroadcast(App.instance)

                    val realm = RealmCache.getInstance()

                    val downloadItem = realm.where(DownloadItem::class.java)
                            .equalTo(DownloadItem.DOWNLOAD_URL, url).findFirst()
                    if (downloadItem != null) {
                        realm.beginTransaction()
                        downloadItem.status = DownloadItem.DOWNLOAD_STATUS_OK
                        downloadItem.filePath = newFile.path
                        realm.commitTransaction()

                    }
                    NotificationUtil.showCompleteNotification(Uri.parse(url), previewUri,
                            if (isUnsplash) null else newFile.absolutePath)
                }
                Log.d(TAG, getString(R.string.completed))
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
                Log.d(TAG, "on handle intent error " + e.message + ",url:" + url)
                NotificationUtil.showErrorNotification(Uri.parse(url), fileName, url, null)

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
                this.outputFile = DownloadUtil.writeToFile(responseBody, file!!.path) {
                    NotificationUtil.showProgressNotification(
                            "MyerSplash",
                            "Downloading...",
                            it, Uri.parse(url), previewUri)
                    RealmCache.getInstance().executeTransaction { realm ->
                        val downloadItem = realm.where(DownloadItem::class.java)
                                .equalTo(DownloadItem.DOWNLOAD_URL, url).findFirst()
                        if (downloadItem != null) {
                            downloadItem.progress = it
                        }
                    }
                }
            }
        }

        val disposable = CloudService.downloadPhoto(url).subscribeWith(observer)
        disposableMap.put(url, disposable)

        return file!!.path
    }
}
