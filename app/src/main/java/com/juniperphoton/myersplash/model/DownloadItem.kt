package com.juniperphoton.myersplash.model

import android.support.annotation.IntDef
import com.google.gson.annotations.SerializedName

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

open class DownloadItem() : RealmObject() {
    @IntDef(DOWNLOAD_STATUS_DOWNLOADING.toLong(), DOWNLOAD_STATUS_OK.toLong(), DOWNLOAD_STATUS_FAILED.toLong())
    @Retention(RetentionPolicy.SOURCE)
    annotation class DownloadStatus

    open var thumbUrl: String? = null

    open var downloadUrl: String? = null

    @PrimaryKey
    open var id: String? = null

    open var progress: Int = 0
        set(value) {
            field = value
            if (this.progress >= 100) {
                status = DOWNLOAD_STATUS_OK
            }
        }

    open var color: Int = 0

    open var status: Int = 0

    open var filePath: String? = null

    open var fileName: String? = null

    @Ignore
    open var lastStatus = DISPLAY_STATUS_NOT_SPECIFIED

    constructor(id: String, thumbUrl: String, downloadUrl: String, fileName: String) : this() {
        this.id = id
        this.thumbUrl = thumbUrl
        this.downloadUrl = downloadUrl
        this.status = DOWNLOAD_STATUS_DOWNLOADING
        this.fileName = fileName
    }

    open fun syncStatus() {
        lastStatus = status
    }

    companion object {
        const val DOWNLOAD_STATUS_DOWNLOADING = 0
        const val DOWNLOAD_STATUS_FAILED = 1
        const val DOWNLOAD_STATUS_OK = 2

        const val DISPLAY_STATUS_NOT_SPECIFIED = -1

        const val ID_KEY = "id"
        const val DOWNLOAD_URL = "downloadUrl"
        const val STATUS_KEY = "status"
    }
}