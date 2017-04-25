package com.juniperphoton.myersplash.model

import android.graphics.Color
import com.google.gson.annotations.SerializedName
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.common.Constant
import com.juniperphoton.myersplash.utils.FileUtil
import com.juniperphoton.myersplash.utils.LocalSettingHelper
import java.io.File
import java.io.Serializable

@Suppress("UNUSED")
class UnsplashImage : Serializable {
    @SerializedName("id")
    val id: String? = null

    @SerializedName("created_at")
    private val mCreatedAt: String? = null

    @SerializedName("color")
    private val mColor: String? = null

    @SerializedName("likes")
    private val mLikes: Int = 0

    @SerializedName("user")
    private val mUser: UnsplashUser? = null

    @SerializedName("urls")
    private val mUrls: ImageUrl? = null

    val listUrl: String?
        get() {
            val choice = LocalSettingHelper.getInt(App.instance, Constant.LOADING_QUALITY_CONFIG_NAME, 0)
            var url: String? = null
            if (mUrls == null) {
                return null
            }
            when (choice) {
                0 -> url = mUrls.mRegular
                1 -> url = mUrls.mSmall
                2 -> url = mUrls.mThumb
            }
            return url
        }

    val downloadUrl: String?
        get() {
            val choice = LocalSettingHelper.getInt(App.instance,
                    Constant.SAVING_QUALITY_CONFIG_NAME, 1)
            var url: String? = null
            when (choice) {
                0 -> url = mUrls!!.mRaw
                1 -> url = mUrls!!.mFull
                2 -> url = mUrls!!.mSmall
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

    val pathForDownload: String
        get() = FileUtil.galleryPath + File.separator + fileNameForDownload

    fun hasDownloaded(): Boolean {
        val path = pathForDownload + ".jpg"
        val file = File(path)
        return file.exists()
    }

    val fileNameForDownload: String
        get() = mUser!!.name + "-" + id + "-" + tagForDownloadUrl

    val themeColor: Int
        get() {
            try {
                var color = Color.parseColor(mColor);
                return color
            } catch (e: Exception) {
                e.printStackTrace()
                return Color.TRANSPARENT
            }
        }

    val userName: String?
        get() = mUser!!.name

    val userHomePage: String?
        get() = mUser!!.homeUrl
}

class ImageUrl : Serializable {
    @SerializedName("raw")
    val mRaw: String? = null

    @SerializedName("full")
    val mFull: String? = null

    @SerializedName("regular")
    val mRegular: String? = null

    @SerializedName("small")
    val mSmall: String? = null

    @SerializedName("thumb")
    val mThumb: String? = null
}