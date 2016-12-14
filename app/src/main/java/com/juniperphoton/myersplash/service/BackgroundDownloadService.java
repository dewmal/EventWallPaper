package com.juniperphoton.myersplash.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;

import com.juniperphoton.myersplash.cloudservice.CloudService;
import com.juniperphoton.myersplash.model.DownloadItem;
import com.juniperphoton.myersplash.utils.DownloadUtil;
import com.juniperphoton.myersplash.utils.NotificationUtil;
import com.juniperphoton.myersplash.utils.ToastService;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.HashMap;

import io.realm.Realm;
import okhttp3.ResponseBody;
import rx.Subscriber;

@SuppressWarnings("UnusedDeclaration")
public class BackgroundDownloadService extends IntentService {
    private static String TAG = BackgroundDownloadService.class.getName();
    private static HashMap<String, Subscriber> subscriptionMap = new HashMap<>();

    public static final String URI_KEY = "URI";
    public static final String NAME_KEY = "NAME";
    public static final String CANCELED_KEY = "CANCELED";
    public static final String FILE_PATH_KEY = "FILE_PATH";
    public static final String CANCEL_NID_KEY = "CANCEL_NID";

    public BackgroundDownloadService(String name) {
        super(name);
    }

    public BackgroundDownloadService() {
        super("BackgroundDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getStringExtra(URI_KEY);
        String fileName = intent.getStringExtra(NAME_KEY);
        boolean canceled = intent.getBooleanExtra(CANCELED_KEY, false);

        int nId = intent.getIntExtra(CANCEL_NID_KEY, NotificationUtil.NOT_ALLOCATED_ID);
        if (nId != NotificationUtil.NOT_ALLOCATED_ID) {
            NotificationUtil.cancelNotificationById(nId);
        }

        if (canceled) {
            Subscriber subscriber = subscriptionMap.get(url);
            if (subscriber != null) {
                subscriber.unsubscribe();
                NotificationUtil.cancelNotification(Uri.parse(url));
                ToastService.sendShortToast("Download has been cancelled");
            }
        } else {
            String filePath = downloadImage(url, fileName);
            NotificationUtil.showProgressNotification("MyerSplash", "Downloading...", 0, filePath, Uri.parse(url));
        }
    }

    protected String downloadImage(final String url, final String fileName) {
        final File file = DownloadUtil.getFileToSave(fileName);
        Subscriber<ResponseBody> subscriber = new Subscriber<ResponseBody>() {
            File outputFile = null;

            @Override
            public void onCompleted() {
                if (outputFile == null) {
                    NotificationUtil.showErrorNotification(Uri.parse(url), fileName, url);
                    Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            DownloadItem downloadItem = realm.where(DownloadItem.class).equalTo("mDownloadUrl", url).findFirst();
                            if (downloadItem != null) {
                                downloadItem.setStatus(DownloadItem.DownloadStatus.Failed);
                            }
                        }
                    });
                } else {
                    Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            DownloadItem downloadItem = realm.where(DownloadItem.class).equalTo("mDownloadUrl", url).findFirst();
                            if (downloadItem != null) {
                                downloadItem.setStatus(DownloadItem.DownloadStatus.Completed);
                                downloadItem.setFilePath(outputFile.getPath());
                            }
                        }
                    });
                    NotificationUtil.showCompleteNotification(Uri.parse(url), Uri.fromFile(outputFile));
                }
                Logger.d(TAG, "Completed");
            }

            @Override
            public void onError(Throwable e) {
                NotificationUtil.showErrorNotification(Uri.parse(url), fileName, url);
                Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        DownloadItem downloadItem = realm.where(DownloadItem.class).equalTo("mDownloadUrl", url).findFirst();
                        if (downloadItem != null) {
                            downloadItem.setStatus(DownloadItem.DownloadStatus.Failed);
                        }
                    }
                });
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                Logger.d(TAG, "outputFile download onNext,size" + responseBody.contentLength());
                this.outputFile = DownloadUtil.writeResponseBodyToDisk(responseBody, file.getPath(), url);
            }
        };
        CloudService.getInstance().downloadPhoto(subscriber, url);
        subscriptionMap.put(url, subscriber);

        return file.getPath();
    }
}
