package com.juniperphoton.myersplash.cloudservice

import com.juniperphoton.myersplash.BuildConfig
import com.juniperphoton.myersplash.model.UnsplashImage
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object CloudService {
    private const val AppKey = BuildConfig.UNSPLASH_APP_KEY

    private val DEFAULT_TIMEOUT = 10

    private val retrofit: Retrofit
    private val photoService: PhotoService
    private val downloadService: DownloadService
    private val builder: OkHttpClient.Builder = OkHttpClient.Builder()

    init {
        builder.connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .addInterceptor(CustomInterceptor())

        retrofit = Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(Request.BASE_URL)
                .build()

        photoService = retrofit.create(PhotoService::class.java)
        downloadService = retrofit.create(DownloadService::class.java)
    }

    fun getPhotos(url: String,
                  page: Int): Observable<MutableList<UnsplashImage>> {
        return photoService.getPhotos(url, page, 10, AppKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getRandomPhotos(url: String): Observable<MutableList<UnsplashImage>> {
        return photoService.getRandomPhotos(url, 10, AppKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getFeaturedPhotos(url: String,
                          page: Int): Observable<MutableList<UnsplashImage>> {
        return photoService
                .getFeaturedPhotos(url, page, 10, AppKey)
                .map { images ->
                    images.map { it.image!! }.toMutableList()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun searchPhotos(url: String,
                     page: Int,
                     query: String): Observable<MutableList<UnsplashImage>> {
        return photoService
                .searchPhotosByQuery(url, page, 10, query, AppKey)
                .map { searchResults ->
                    searchResults.list!!
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun downloadPhoto(url: String): Observable<ResponseBody> {
        return downloadService
                .downloadFileWithDynamicUrlSync(url).timeout(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
    }
}
