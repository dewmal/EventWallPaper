package com.juniperphoton.myersplash.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.juniperphoton.myersplash.base.App;
import com.juniperphoton.myersplash.cloudservice.CloudService;
import com.juniperphoton.myersplash.utils.DownloadUtil;
import com.juniperphoton.myersplash.utils.NotificationUtil;

import okhttp3.ResponseBody;
import rx.Subscriber;

public class BackgrdDownloadService extends IntentService {
    private static String TAG = BackgrdDownloadService.class.getName();

    public BackgrdDownloadService(String name) {
        super(name);
    }

    public BackgrdDownloadService() {
        super("BackgrdDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getStringExtra("url");
        String fileName = intent.getStringExtra("name");
        downloadImage(url, fileName);
        NotificationUtil.sendNotification("MyerSplash", "Downloading...", false);
    }

    protected void downloadImage(final String url, final String fileName) {
        new AsyncTask<Void, Long, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                CloudService.getInstance().downloadPhoto(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, e.getMessage());
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        DownloadUtil.writeResponseBodyToDisk(responseBody, fileName);
                    }
                }, url);
                return null;
            }
        }.execute();
    }
}
