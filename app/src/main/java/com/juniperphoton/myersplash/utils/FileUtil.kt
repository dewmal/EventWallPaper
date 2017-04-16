package com.juniperphoton.myersplash.utils

import android.net.Uri
import android.os.Environment
import android.util.Log
import com.facebook.binaryresource.FileBinaryResource
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory
import com.facebook.imagepipeline.core.ImagePipelineFactory
import com.facebook.imagepipeline.request.ImageRequest
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.extension.getFolderLengthInMb
import java.io.File

object FileUtil {
    val galleryPath: String?
        get() {
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

    val sharePath: String?
        get() {
            val gallery = galleryPath
            val folder = File(gallery, "Shared")
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    return null
                }
            }
            return folder.absolutePath
        }

    fun getCachedFile(url: String): File? {
        val cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(
                ImageRequest.fromUri(Uri.parse(url)), null)

        var localFile: File? = null

        if (cacheKey != null) {
            if (ImagePipelineFactory.getInstance().mainFileCache.hasKey(cacheKey)) {
                val resource = ImagePipelineFactory.getInstance().mainFileCache.getResource(cacheKey)
                localFile = (resource as FileBinaryResource).file
            }
        }

        return localFile
    }

    fun clearFilesToShared() {
        val folder = File(sharePath)
        if (folder.exists() && folder.isDirectory) {
            Log.d("size", "${folder.getFolderLengthInMb()}")
            if (folder.getFolderLengthInMb() >= 5) {
                folder.deleteOnExit()
            }
        }
    }
}