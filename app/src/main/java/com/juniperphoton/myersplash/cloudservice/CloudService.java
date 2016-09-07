package com.juniperphoton.myersplash.cloudservice;

import com.juniperphoton.myersplash.model.UnsplashCategory;
import com.juniperphoton.myersplash.model.UnsplashImage;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CloudService {
    private static String AppKey = "403d9934ce4bb8dbef44765692144e8c6fac6d2698950cb40b07397d6c6635fe";

    public static String baseUrl = "https://api.unsplash.com/";

//    private static String getNewImages = "https://api.unsplash.com/photos/?";
//
//    private static String searchImages = "https://api.unsplash.com/search/photos?";
//
//    private static String getCategories = "https://api.unsplash.com/categories?";
//
//    private static String getFeaturedImages = "https://api.unsplash.com/collections/featured?";

    private static final int DEFAULT_TIMEOUT = 10;

    private Retrofit baseRetrofit;
    private Retrofit categoryRetrofit;
    private CategoryService categoryService;
    private PhotoService photoService;

    private CloudService() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        baseRetrofit = new Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(baseUrl)
                .build();

        categoryService = baseRetrofit.create(CategoryService.class);
        photoService = baseRetrofit.create(PhotoService.class);
    }

    private String getUrlWithParam(String url) {
        return url + "client_id=" + AppKey;
    }

    private static class SingletonHolder {
        private static final CloudService INSTANCE = new CloudService();
    }

    public static CloudService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void getCategories(Subscriber<List<UnsplashCategory>> subscriber) {
        Observable<List<UnsplashCategory>> observable = categoryService.getCategories(AppKey);
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void getPhotos(Subscriber<List<UnsplashImage>> subscriber, String url, int page) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        categoryRetrofit = new Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(url)
                .build();
        photoService = categoryRetrofit.create(PhotoService.class);

        Observable<List<UnsplashImage>> observable = photoService.getPhotos(page, 10, AppKey);
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }
}
