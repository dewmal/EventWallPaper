package com.juniperphoton.myersplash.cloudservice

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import rx.Observable

interface DownloadService {
    @Streaming
    @GET
    fun downloadFileWithDynamicUrlSync(@Url fileUrl: String): Observable<ResponseBody>
}
