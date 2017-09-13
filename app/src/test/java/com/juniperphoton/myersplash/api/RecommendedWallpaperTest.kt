package com.juniperphoton.myersplash.api

import com.juniperphoton.myersplash.cloudservice.APIException
import com.juniperphoton.myersplash.cloudservice.CloudService
import com.juniperphoton.myersplash.cloudservice.Request
import com.juniperphoton.myersplash.provider.WallpaperWidgetProvider
import okhttp3.ResponseBody
import org.junit.Test
import rx.observers.TestSubscriber

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
        val testSubscriber = TestSubscriber<ResponseBody>()
        CloudService.downloadPhoto(thumbUrl, testSubscriber)
        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertNoErrors()
    }

    @Test
    fun testCantDownloadRecommendedThumb() {
        val testSubscriber = TestSubscriber<ResponseBody>()
        CloudService.downloadPhoto(invalidUrl, testSubscriber)
        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertError(APIException::class.java)
    }

    @Test
    fun testRecommendedLarge() {
        val testSubscriber = TestSubscriber<ResponseBody>()
        CloudService.downloadPhoto(largeUrl, testSubscriber)
        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertNoErrors()
    }
}