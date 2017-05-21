package com.juniperphoton.myersplash.cloudservice

import com.juniperphoton.myersplash.model.UnsplashCategory

import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable

interface CategoryService {
    @GET("categories")
    fun getCategories(@Query("client_id") id: String): Observable<MutableList<UnsplashCategory?>?>
}
