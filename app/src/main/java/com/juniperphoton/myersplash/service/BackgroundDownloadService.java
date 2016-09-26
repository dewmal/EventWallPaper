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
        String url = intent.getStringExtra("URI");
        String fileName = intent.getStringExtra("NAME");
        int nId = intent.getIntExtra("NID", -1);
        if (nId != -1) {
            if (nId != NotificationUtil.NOT_ALLOCATED_ID) {
                NotificationUtil.cancelNotificationById(nId);
            }
        }

        downloadImage(url, fileName);
        NotificationUtil.showProgressNotification("MyerSplash", "Downloading...", 0, Uri.parse(url));
    }

    protected void downloadImage(final String url, final String fileName) {
        CloudService.getInstance().downloadPhoto(new Subscriber<ResponseBody>() {
            File outputFile = null;

            @Override
            public void onCompleted() {
                if (outputFile == null) {
                    NotificationUtil.showErrorNotification(Uri.parse(url), fileName, url);
                } else {
                    NotificationUtil.showCompleteNotification(Uri.parse(url), Uri.fromFile(outputFile));
                }
                Logger.d(TAG, "Completed");
            }

            @Override
            public void onError(Throwable e) {
                NotificationUtil.showErrorNotification(Uri.parse(url), fileName, url);
                Logger.d(TAG, "Error");
                Log.d(TAG, "onError," + e.getMessage() + "," + url);
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                Logger.d(TAG, "outputFile download onNext,size" + responseBody.contentLength());
                this.outputFile = DownloadUtil.writeResponseBodyToDisk(responseBody, fileName, url);
            }
        }, url);
    }
}
