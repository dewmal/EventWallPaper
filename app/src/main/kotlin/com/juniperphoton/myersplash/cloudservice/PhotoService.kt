package com.juniperphoton.myersplash.cloudservice

import com.juniperphoton.myersplash.model.SearchResult
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.model.UnsplashImageFeatured

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import rx.Observable

interface PhotoService {
    @GET
    fun getPhotos(@Url url: String,
                  @Query("page") page: Int,
                  @Query("per_page") per_page: Int,
                  @Query("client_id") id: String): Observable<MutableList<UnsplashImage>>

    @GET
    fun getFeaturedPhotos(@Url url: String,
                          @Query("page") page: Int,
                          @Query("per_page") per_page: Int,
                          @Query("client_id") id: String): Observable<MutableList<UnsplashImageFeatured>>

    @GET
    fun searchPhotosByQuery(@Url url: String,
                            @Query("page") page: Int,
                            @Query("per_page") per_page: Int,
                            @Query("query") query: String,
                            @Query("client_id") id: String): Observable<SearchResult>

    @GET
    fun getRandomPhotos(@Url url: String,
                        @Query("count") count: Int,
                        @Query("client_id") id: String): Observable<MutableList<UnsplashImage>>
}
