package com.juniperphoton.myersplash.cloudservice

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DownloadService {
    @Streaming
    @GET
    fun downloadFileWithDynamicUrlSync(@Url fileUrl: String): Observable<ResponseBody>

    @GET
    fun reportDownload(@Url url: String, @Query("client_id") key: String): Observable<ResponseBody>
}