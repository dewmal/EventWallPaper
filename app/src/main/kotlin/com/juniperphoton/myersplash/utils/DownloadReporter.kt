package com.juniperphoton.myersplash.utils

import com.juniperphoton.myersplash.cloudservice.CloudService
import okhttp3.ResponseBody

object DownloadReporter {
    private const val TAG = "DownloadReporter"

    fun report(downloadLocation: String?) {
        val url = downloadLocation ?: return

        CloudService.reportDownload(url)
                .subscribe(object : ResponseObserver<ResponseBody>() {
                    override fun onNext(data: ResponseBody) {
                        super.onNext(data)
                        Pasteur.info(TAG, "successfully report $url")
                    }
                })
    }
}