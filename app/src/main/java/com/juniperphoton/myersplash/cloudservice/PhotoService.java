package com.juniperphoton.myersplash.cloudservice;

import com.juniperphoton.myersplash.model.UnsplashCategory;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.model.UnsplashImageFeatured;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

public interface PhotoService {
    @GET
    Observable<List<UnsplashImage>> getPhotos(@Url String url, @Query("page") int page, @Query("per_page") int per_page, @Query("client_id") String id);
    @GET
    Observable<List<UnsplashImageFeatured>> getFeaturedPhotos(@Url String url, @Query("page") int page, @Query("per_page") int per_page, @Query("client_id") String id);
}
