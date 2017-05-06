package com.juniperphoton.myersplash.cloudservice

import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.model.UnsplashImage

import java.util.ArrayList
import java.util.concurrent.TimeUnit

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

object CloudService {
    private val AppKey = "403d9934ce4bb8dbef44765692144e8c6fac6d2698950cb40b07397d6c6635fe"

    var BASE_URL = "https://api.unsplash.com/"
    var PHOTO_URL = "https://api.unsplash.com/photos?"
    var FEATURED_PHOTO_URL = "https://api.unsplash.com/collections/featured?"
    var RANDOM_PHOTOS_URL = "https://api.unsplash.com/photos/random?"
    var SEARCH_URL = "https://api.unsplash.com/search/photos?"

    private val DEFAULT_TIMEOUT = 10

    private val retrofit: Retrofit
    private val categoryService: CategoryService
    private val photoService: PhotoService
    private val downloadService: DownloadService
    private val builder: OkHttpClient.Builder = OkHttpClient.Builder()

    init {
        builder.connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)

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

    fun getCategories(subscriber: Subscriber<MutableList<UnsplashCategory?>?>) {
        val observable = categoryService.getCategories(AppKey)
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber)
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
            if (images != null) {
                for (img in images) {
                    contentImages.add(img?.image)
                }
            }
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
        return observable.subscribeOn(Schedulers.io())
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
