package com.juniperphoton.myersplash.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.juniperphoton.myersplash.base.App;
import com.juniperphoton.myersplash.cloudservice.CloudService;
import com.juniperphoton.myersplash.utils.DownloadUtil;
import com.juniperphoton.myersplash.utils.NotificationUtil;
import com.orhanobut.logger.Logger;

import okhttp3.ResponseBody;
import rx.Subscriber;

@SuppressWarnings("UnusedDeclaration")
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
        NotificationUtil.sendNotification("MyerSplash", "Downloading...", false, Uri.parse(url));
    }

    protected void downloadImage(final String url, final String fileName) {
        new AsyncTask<Void, Long, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
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

                        boolean ok = DownloadUtil.writeResponseBodyToDisk(responseBody, fileName);
                        if (ok) {
                            NotificationUtil.sendNotification("MyerSplash", "Saved :D", true, Uri.parse(url));
                        }
                    }
                }, url);
                return null;
            }
        }.execute();
    }
}
