package com.juniperphoton.myersplash.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.juniperphoton.myersplash.cloudservice.CloudService;
import com.juniperphoton.myersplash.utils.DownloadUtil;
import com.juniperphoton.myersplash.utils.NotificationUtil;
import com.orhanobut.logger.Logger;

import java.io.File;

import okhttp3.ResponseBody;
import rx.Subscriber;

@SuppressWarnings("UnusedDeclaration")
public class BackgroundDownloadService extends IntentService {
    private static String TAG = BackgroundDownloadService.class.getName();

    public BackgroundDownloadService(String name) {
        super(name);
    }

    public BackgroundDownloadService() {
        super("BackgroundDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getStringExtra("url");
        String fileName = intent.getStringExtra("name");
        downloadImage(url, fileName);
        NotificationUtil.showProgressNotification("MyerSplash", "Downloading...", 0, Uri.parse(url));
    }

    protected void downloadImage(final String url, final String fileName) {
        CloudService.getInstance().downloadPhoto(new Subscriber<ResponseBody>() {
            @Override
            public void onCompleted() {
                Logger.d(TAG, "Completed");
            }

            @Override
            public void onError(Throwable e) {
                NotificationUtil.showErrorNotification(Uri.parse(url));
                Logger.d(TAG, "Error");
                Log.d(TAG, "onError," + e.getMessage() + "," + url);
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                Log.d(TAG, "file download onNext,size" + responseBody.contentLength());
                File file = DownloadUtil.writeResponseBodyToDisk(responseBody, fileName, url);
            }
        }, url);
    }
}
