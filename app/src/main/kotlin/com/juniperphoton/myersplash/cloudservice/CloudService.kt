package com.juniperphoton.myersplash.cloudservice

import com.juniperphoton.myersplash.BuildConfig
import com.juniperphoton.myersplash.model.UnsplashImage
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

object CloudService {
    private const val AppKey = BuildConfig.UNSPLASH_APP_KEY

    const val BASE_URL = "https://api.unsplash.com/"
    const val PHOTO_URL = "https://api.unsplash.com/photos?"
    const val FEATURED_PHOTO_URL = "https://api.unsplash.com/collections/featured?"
    const val RANDOM_PHOTOS_URL = "https://api.unsplash.com/photos/random?"
    const val SEARCH_URL = "https://api.unsplash.com/search/photos?"
    const val AUTO_CHANGE_WALLPAPER = "http://juniperphoton.net/myersplash/wallpapers/"
    const val AUTO_CHANGE_WALLPAPER_THUMB = "http://juniperphoton.net/myersplash/wallpapers/thumbs/"

    private val DEFAULT_TIMEOUT = 10

    private val retrofit: Retrofit
    private val categoryService: CategoryService
    private val photoService: PhotoService
    private val downloadService: DownloadService
    private val builder: OkHttpClient.Builder = OkHttpClient.Builder()

    init {
        builder.connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .addInterceptor(CustomInterceptor())

        retrofit = Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build()

        categoryService = retrofit.create(CategoryService::class.java)
        photoService = retrofit.create(PhotoService::class.java)
        downloadService = retrofit.create(DownloadService::class.java)
    }

    fun getPhotos(subscriber: Subscriber<MutableList<UnsplashImage?>?>, url: String, page: Int) {
        val observable = photoService.getPhotos(url, page, 10, AppKey)
        subscribe(observable, subscriber)
    }

    fun getRandomPhotos(subscriber: Subscriber<MutableList<UnsplashImage?>?>, url: String) {
        val observable = photoService.getRandomPhotos(url, 10, AppKey)
        subscribe(observable, subscriber)
    }

    fun getFeaturedPhotos(subscriber: Subscriber<MutableList<UnsplashImage?>?>, url: String, page: Int) {
        val observableF = photoService.getFeaturedPhotos(url, page, 10, AppKey)
        val observable: Observable<MutableList<UnsplashImage?>?> = observableF.map { images ->
            val contentImages: ArrayList<UnsplashImage?> = ArrayList()
            images?.mapTo(contentImages) { it?.image }
            contentImages
        }
        subscribe(observable, subscriber)
    }

    fun searchPhotos(subscriber: Subscriber<MutableList<UnsplashImage?>?>, url: String, page: Int, query: String) {
        val observableF = photoService.searchPhotosByQuery(url, page, 10, query, AppKey)
        val observable: Observable<MutableList<UnsplashImage?>?> = observableF.map { searchResults -> searchResults?.list }
        subscribe(observable, subscriber)
    }

    fun downloadPhoto(subscriber: Subscriber<ResponseBody>, url: String): Subscription {
        val observable = downloadService.downloadFileWithDynamicUrlSync(url)
        return observable.timeout(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(subscriber)
    }

    private fun subscribe(observable: Observable<MutableList<UnsplashImage?>?>, subscriber: Subscriber<MutableList<UnsplashImage?>?>) {
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber)
    }
}
