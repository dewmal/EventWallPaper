package com.juniperphoton.myersplash.api

import com.juniperphoton.myersplash.broadcastreceiver.WallpaperWidgetProvider
import com.juniperphoton.myersplash.cloudservice.CloudService
import com.juniperphoton.myersplash.cloudservice.Request
import io.reactivex.observers.TestObserver
import okhttp3.ResponseBody
import org.junit.Test

class RecommendedWallpaperTest {
    private val thumbUrl: String
        get() =
            "${Request.AUTO_CHANGE_WALLPAPER_THUMB}${WallpaperWidgetProvider.dateString}.jpg"

    private val largeUrl: String
        get() = "${Request.AUTO_CHANGE_WALLPAPER}${WallpaperWidgetProvider.dateString}.jpg"

    private val invalidUrl: String
        get() = "${Request.AUTO_CHANGE_WALLPAPER_THUMB}nothumb.jpg"

    @Test
    fun testRecommendedThumb() {
        val observer = TestObserver<ResponseBody>()
        CloudService.downloadPhoto(thumbUrl).subscribe(observer)
        observer.awaitTerminalEvent()
        observer.assertNoErrors()
    }

    @Test
    fun testCantDownloadRecommendedThumb() {
        val observer = TestObserver<ResponseBody>()
        CloudService.downloadPhoto(invalidUrl).subscribe(observer)
        observer.awaitTerminalEvent()
        observer.assertNoErrors()
    }

    @Test
    fun testRecommendedLarge() {
        val observer = TestObserver<ResponseBody>()
        CloudService.downloadPhoto(largeUrl).subscribe(observer)
        observer.awaitTerminalEvent()
        observer.assertNoErrors()
    }
}