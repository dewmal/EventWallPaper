package com.juniperphoton.myersplash.cloudservice;

import com.juniperphoton.myersplash.model.SearchResult;
import com.juniperphoton.myersplash.model.UnsplashCategory;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.model.UnsplashImageFeatured;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class CloudService {
    private static String AppKey = "403d9934ce4bb8dbef44765692144e8c6fac6d2698950cb40b07397d6c6635fe";

    public static String BASE_URL = "https://api.unsplash.com/";
    public static String PHOTO_URL = "https://api.unsplash.com/photos?";
    public static String FEATURED_PHOTO_URL = "https://api.unsplash.com/collections/featured?";
    public static String RANDOM_PHOTOS_URL = "https://api.unsplash.com/photos/random?";
    public static String SEARCH_URL = "https://api.unsplash.com/search/photos?";

    private static final int DEFAULT_TIMEOUT = 10;

    private Retrofit retrofit;
    private CategoryService categoryService;
    private PhotoService photoService;
    private DownloadService downloadService;
    private OkHttpClient.Builder builder;

    private CloudService() {
        builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        retrofit = new Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();

        categoryService = retrofit.create(CategoryService.class);
        photoService = retrofit.create(PhotoService.class);
        downloadService = retrofit.create(DownloadService.class);
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
        Observable<List<UnsplashImage>> observable = photoService.getPhotos(url, page, 10, AppKey);
        subscribe(observable, subscriber);
    }

    public void getRandomPhotos(Subscriber<List<UnsplashImage>> subscriber, String url) {
        Observable<List<UnsplashImage>> observable = photoService.getRandomPhotos(url, 10, AppKey);
        subscribe(observable, subscriber);
    }

    public void getFeaturedPhotos(Subscriber<List<UnsplashImage>> subscriber, String url, int page) {
        Observable<List<UnsplashImageFeatured>> observableF = photoService.getFeaturedPhotos(url, page, 10, AppKey);
        Observable<List<UnsplashImage>> observable = observableF.map(new Func1<List<UnsplashImageFeatured>, List<UnsplashImage>>() {
            @Override
            public List<UnsplashImage> call(List<UnsplashImageFeatured> images) {
                ArrayList<UnsplashImage> contentImages = new ArrayList<>();
                for (UnsplashImageFeatured img : images) {
                    contentImages.add(img.getImage());
                }
                return contentImages;
            }
        });
        subscribe(observable, subscriber);
    }

    public void searchPhotos(Subscriber<List<UnsplashImage>> subscriber, String url, int page, String query) {
        Observable<SearchResult> observableF = photoService.searchPhotosByQuery(url, page, 10, query, AppKey);
        Observable<List<UnsplashImage>> observable = observableF.map(new Func1<SearchResult, List<UnsplashImage>>() {
            @Override
            public List<UnsplashImage> call(SearchResult searchResults) {
                return searchResults.getList();
            }
        });
        subscribe(observable, subscriber);
    }

    private void subscribe(Observable<List<UnsplashImage>> observable, Subscriber<List<UnsplashImage>> subscriber) {
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public Subscription downloadPhoto(Subscriber<ResponseBody> subscriber, String url) {
        Observable<ResponseBody> observable = downloadService.downloadFileWithDynamicUrlSync(url);
        return observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(subscriber);
    }
}
