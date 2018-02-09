package com.juniperphoton.myersplash.model

import android.graphics.Color
import com.google.gson.annotations.SerializedName
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.broadcastreceiver.WallpaperWidgetProvider
import com.juniperphoton.myersplash.cloudservice.Request.ME_HOME_PAGE
import com.juniperphoton.myersplash.utils.FileUtil
import com.juniperphoton.myersplash.utils.LocalSettingHelper
import io.reactivex.Observable
import java.io.File
import java.io.Serializable

@Suppress("unused")
class UnsplashImage : Serializable {
    companion object {
        private val savingQualitySettingsKey = App.instance.getString(R.string.preference_key_saving_quality)
        private val listQualitySettingsKey = App.instance.getString(R.string.preference_key_list_quality)

        fun createTodayImage(): UnsplashImage {
            return UnsplashImage().apply {
                isUnsplash = false
                id = WallpaperWidgetProvider.DATE_STRING
                urls = ImageUrl().apply {
                    raw = WallpaperWidgetProvider.DOWNLOAD_URL
                    full = WallpaperWidgetProvider.DOWNLOAD_URL
                    regular = WallpaperWidgetProvider.THUMB_URL
                    small = WallpaperWidgetProvider.THUMB_URL
                    thumb = WallpaperWidgetProvider.THUMB_URL
                }
                user = UnsplashUser().apply {
                    val authorName = App.instance.getString(R.string.author_default_name)
                    userName = authorName
                    name = authorName
                    links = ProfileUrl().apply {
                        html = ME_HOME_PAGE
                    }
                }
            }
        }
    }

    @SerializedName("id")
    var id: String? = null
        private set

    @SerializedName("created_at")
    private val createdAt: String? = null

    @SerializedName("color")
    private val color: String? = null

    @SerializedName("likes")
    private val likes: Int = 0

    @SerializedName("user")
    private var user: UnsplashUser? = null

    @SerializedName("urls")
    private var urls: ImageUrl? = null

    @SerializedName("links")
    private var links: ImageLinks? = null

    val downloadLocationLink: String?
        get() = links?.downloadLocation

    var isUnsplash: Boolean = true
        private set

    val pathForDownload: String
        get() = FileUtil.galleryPath + File.separator + fileNameForDownload

    val fileNameForDownload: String
        get() = "${user!!.name} - $id - $tagForDownloadUrl"

    val themeColor: Int
        get() = try {
            Color.parseColor(color)
        } catch (e: Exception) {
            Color.BLACK
        }

    val userName: String?
        get() = user?.name

    val userHomePage: String?
        get() = user?.homeUrl

    val listUrl: String?
        get() {
            val urls = urls ?: return null
            val choice = LocalSettingHelper.getInt(App.instance, listQualitySettingsKey, 0)
            return when (choice) {
                0 -> urls.regular
                1 -> urls.small
                2 -> urls.thumb
                else -> null
            }
        }

    val downloadUrl: String?
        get() {
            val urls = urls ?: return null
            val choice = LocalSettingHelper.getInt(App.instance, savingQualitySettingsKey, 1)
            return when (choice) {
                0 -> urls.raw
                1 -> urls.full
                2 -> urls.small
                else -> null
            }
        }

    fun checkDownloaded(): Observable<Boolean> {
        return Observable.create { s ->
            try {
                val path = pathForDownload + ".jpg"
                val file = File(path)
                val existed = file.exists()
                s.onNext(existed)
                s.onComplete()
            } catch (e: Exception) {
                s.onError(e)
            }
        }
    }

    private val tagForDownloadUrl: String
        get() {
            val choice = LocalSettingHelper.getInt(App.instance, savingQualitySettingsKey, 1)
            return when (choice) {
                0 -> "raw"
                1 -> "regular"
                2 -> "small"
                else -> ""
            }
        }
}

class ImageLinks : Serializable {
    @SerializedName("download_location")
    var downloadLocation: String? = null
}

class ImageUrl : Serializable {
    @SerializedName("raw")
    var raw: String? = null

    @SerializedName("full")
    var full: String? = null

    @SerializedName("regular")
    var regular: String? = null

    @SerializedName("small")
    var small: String? = null

    @SerializedName("thumb")
    var thumb: String? = null
}