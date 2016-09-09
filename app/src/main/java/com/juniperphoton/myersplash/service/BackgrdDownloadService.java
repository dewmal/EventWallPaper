package com.juniperphoton.myersplash.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.juniperphoton.myersplash.cloudservice.CloudService;
import com.juniperphoton.myersplash.common.Constant;
import com.juniperphoton.myersplash.utils.DownloadUtils;

import okhttp3.ResponseBody;
import rx.Subscriber;

public class BackgrdDownloadService extends IntentService {
    private static String TAG = BackgrdDownloadService.class.getName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
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
        donwloadImage(url, fileName);
    }

    protected void donwloadImage(final String url, final String fileName) {
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
                        DownloadUtils.writeResponseBodyToDisk(responseBody, fileName);
                    }
                }, url);
                return null;
            }
        }.execute();
    }
}
