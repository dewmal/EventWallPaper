package com.juniperphoton.myersplash.cloudservice;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

public interface DownloadService {
    @Streaming
    @GET
    Observable<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);
}
