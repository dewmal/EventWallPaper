package com.juniperphoton.myersplash.utils

import android.net.Uri
import android.os.Environment
import android.util.Log
import com.facebook.binaryresource.FileBinaryResource
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory
import com.facebook.imagepipeline.core.ImagePipelineFactory
import com.facebook.imagepipeline.request.ImageRequest
import com.juniperphoton.myersplash.App
import java.io.*
import java.lang.Exception

object FileUtil {
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

    fun clearFilesToShared() {
        var folder = File(sharePath)
        if (folder.exists() && folder.isDirectory) {
            Log.d("size", "${folder!!.getFolderLengthInMb()}")
            if (folder!!.getFolderLengthInMb() >= 5) {
                folder.deleteOnExit()
            }
        }
    }

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
            var gallery = galleryPath
            var folder = File(gallery, "Shared")
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    return null
                }
            }
            return folder.absolutePath
        }
}