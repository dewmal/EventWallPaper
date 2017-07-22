package com.juniperphoton.myersplash.model

import android.graphics.Color
import com.google.gson.annotations.SerializedName
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.common.Constant
import com.juniperphoton.myersplash.provider.WallpaperWidgetProvider
import com.juniperphoton.myersplash.utils.FileUtil
import com.juniperphoton.myersplash.utils.LocalSettingHelper
import java.io.File
import java.io.Serializable

@Suppress("unused")
class UnsplashImage : Serializable {
    companion object {
        fun createRecommendedImage(): UnsplashImage {
            return UnsplashImage().apply {
                isUnsplash = false
                id = WallpaperWidgetProvider.dateString
                urls = ImageUrl().apply {
                    raw = WallpaperWidgetProvider.downloadUrl
                    full = WallpaperWidgetProvider.downloadUrl
                    regular = WallpaperWidgetProvider.thumbUrl
                    small = WallpaperWidgetProvider.thumbUrl
                    thumb = WallpaperWidgetProvider.thumbUrl
                }
                user = UnsplashUser().apply {
                    userName = "JuniperPhoton"
                    name = "JuniperPhoton"
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
        private set

    var isUnsplash: Boolean = true
        private set

    val pathForDownload: String
        get() = FileUtil.galleryPath + File.separator + fileNameForDownload

    val fileNameForDownload: String
        get() = "${user!!.name} - $id - $tagForDownloadUrl"

    val themeColor: Int
        get() {
            try {
                return Color.parseColor(color)
            } catch (e: Exception) {
                e.printStackTrace()
                return Color.TRANSPARENT
            }
        }

    val userName: String?
        get() = user!!.name

    val userHomePage: String?
        get() = user!!.homeUrl

    val listUrl: String?
        get() {
            val choice = LocalSettingHelper.getInt(App.instance, Constant.LOADING_QUALITY_CONFIG_NAME, 0)
            var url: String? = null
            if (urls == null) {
                return null
            }
            when (choice) {
                0 -> url = urls!!.regular
                1 -> url = urls!!.small
                2 -> url = urls!!.thumb
            }
            return url
        }

    val downloadUrl: String?
        get() {
            val choice = LocalSettingHelper.getInt(App.instance,
                    Constant.SAVING_QUALITY_CONFIG_NAME, 1)
            var url: String? = null
            when (choice) {
                0 -> url = urls!!.raw
                1 -> url = urls!!.full
                2 -> url = urls!!.small
            }
            return url
        }

    private val tagForDownloadUrl: String
        get() {
            val choice = LocalSettingHelper.getInt(App.instance, Constant.SAVING_QUALITY_CONFIG_NAME, 1)
            var tag = ""
            when (choice) {
                0 -> tag = "raw"
                1 -> tag = "regular"
                2 -> tag = "small"
            }
            return tag
        }

    fun hasDownloaded(): Boolean {
        val path = pathForDownload + ".jpg"
        val file = File(path)
        return file.exists()
    }
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