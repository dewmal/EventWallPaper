package com.juniperphoton.myersplash.cloudservice;

import com.juniperphoton.myersplash.model.UnsplashCategory;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface CategoryService {
    @GET("categories")
    Observable<List<UnsplashCategory>> getCategories(@Query("client_id") String id);
}
