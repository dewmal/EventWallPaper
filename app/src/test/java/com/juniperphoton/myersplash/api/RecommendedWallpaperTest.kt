package com.juniperphoton.myersplash.api

import com.juniperphoton.myersplash.cloudservice.APIException
import com.juniperphoton.myersplash.cloudservice.CloudService
import com.juniperphoton.myersplash.provider.WallpaperWidgetProvider
import okhttp3.ResponseBody
import org.junit.Test
import rx.observers.TestSubscriber

class RecommendedWallpaperTest {
    val thumbUrl: String
        get() {
            return "${CloudService.AUTO_CHANGE_WALLPAPER_THUMB}${WallpaperWidgetProvider.dateString}.jpg"
        }

    val largeUrl: String
        get() {
            return "${CloudService.AUTO_CHANGE_WALLPAPER}${WallpaperWidgetProvider.dateString}.jpg"
        }

    val invalidUrl: String
        get() {
            return "${CloudService.AUTO_CHANGE_WALLPAPER_THUMB}nothumb.jpg"
        }

    @Test
    fun testRecommendedThumb() {
        val testSubscriber = TestSubscriber<ResponseBody>()
        CloudService.downloadPhoto(testSubscriber, thumbUrl)
        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertNoErrors()
    }

    @Test
    fun testCantDownloadRecommendedThumb() {
        val testSubscriber = TestSubscriber<ResponseBody>()
        CloudService.downloadPhoto(testSubscriber, invalidUrl)
        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertError(APIException::class.java)
    }

    @Test
    fun testRecommendedLarge() {
        val testSubscriber = TestSubscriber<ResponseBody>()
        CloudService.downloadPhoto(testSubscriber, largeUrl)
        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertNoErrors()
    }
}