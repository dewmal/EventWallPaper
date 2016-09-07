package com.juniperphoton.myersplash.cloudservice;

import com.juniperphoton.myersplash.model.UnsplashCategory;
import com.juniperphoton.myersplash.model.UnsplashImage;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface PhotoService {
    @GET("photos")
    Observable<List<UnsplashImage>> getPhotos(@Query("page") int page, @Query("per_page") int per_page, @Query("client_id") String id);
}
