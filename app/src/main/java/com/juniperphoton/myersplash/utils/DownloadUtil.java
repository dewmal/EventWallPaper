package com.juniperphoton.myersplash.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.base.App;
import com.juniperphoton.myersplash.event.DownloadStartedEvent;
import com.juniperphoton.myersplash.model.DownloadItem;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.service.BackgroundDownloadService;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.Date;

import io.realm.Realm;
import okhttp3.ResponseBody;

public class DownloadUtil {
    private static String TAG = "DownloadUtil";

    public static File writeResponseBodyToDisk(ResponseBody body, String fileUri, final String downloadUrl) {
        try {
            File fileToSave = new File(fileUri);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                long startTime = new Date().getTime();

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(fileToSave);

                byte[] buffer = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                int progressToReport = 0;

                while (true) {
                    int read = inputStream.read(buffer);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(buffer, 0, read);

                    fileSizeDownloaded += read;

                    int progress = (int) (fileSizeDownloaded / (double) fileSize * 100);
                    if (progress - progressToReport >= 5) {
                        progressToReport = progress;
                        final int progressToDisplay = progressToReport;
                        NotificationUtil.showProgressNotification("MyerSplash", "Downloading...",
                                progressToReport, fileUri, Uri.parse(downloadUrl));
                        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                DownloadItem downloadItem = realm.where(DownloadItem.class)
                                        .equalTo(DownloadItem.DOWNLOAD_URL, downloadUrl).findFirst();
                                if (downloadItem != null) {
                                    downloadItem.setProgress(progressToDisplay);
                                }
                            }
                        });
                    }
                }
                long endTime = new Date().getTime();

                Log.d(TAG, "time spend=" + String.valueOf(endTime - startTime));

                outputStream.flush();

                return fileToSave;
            } catch (InterruptedIOException e0) {
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }

                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            ToastService.sendShortToast(e.getMessage());
            return null;
        }
    }

    public static File getFileToSave(String expectedName) {
        String galleryPath = getGalleryPath();
        if (galleryPath == null) {
            return null;
        }
        File folder = new File(getGalleryPath());
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return new File(folder + File.separator + expectedName);
    }

    /**
     * 获得媒体库的文件夹
     *
     * @return 路径
     */
    public static String getGalleryPath() {
        File mediaStorageDir;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (null == path) return "";
            mediaStorageDir = new File(path, "MyerSplash");
        } else {
            String extStorageDirectory = App.getInstance().getFilesDir().getAbsolutePath();
            mediaStorageDir = new File(extStorageDirectory, "MyerSplash");
        }

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        return mediaStorageDir.getAbsolutePath();
    }

    public static boolean copyFile(File srcF, File destF) {
        if (!destF.exists()) {
            try {
                if (!destF.createNewFile()) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            try {
                inputStream = new FileInputStream(srcF);
                outputStream = new FileOutputStream(destF);

                byte[] fileReader = new byte[4096];
                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);
                }
                outputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }

                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void cancelDownload(Context context, UnsplashImage image) {
        Intent intent = new Intent(App.getInstance(), BackgroundDownloadService.class);
        intent.putExtra(Params.CANCELED_KEY, true);
        intent.putExtra(Params.URL_KEY, image.getDownloadUrl());
        context.startService(intent);
    }

    public static void checkAndDownload(final Activity context, final UnsplashImage image) {
        if (!RequestUtil.check(context)) {
            ToastService.sendShortToast(context.getString(R.string.no_permission));
            return;
        }
        if (!NetworkUtil.usingWifi(context)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.attention);
            builder.setMessage(R.string.wifi_attention_content);
            builder.setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startDownloadService(context, image);
                    EventBus.getDefault().post(new DownloadStartedEvent(image.getId()));
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } else {
            startDownloadService(context, image);
            EventBus.getDefault().post(new DownloadStartedEvent(image.getId()));
        }
    }

    public static DownloadItem getDownloadItemById(String id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        DownloadItem item = realm.where(DownloadItem.class)
                .equalTo(DownloadItem.ID_KEY, id)
                .findFirst();
        realm.commitTransaction();
        return item;
    }

    private static void startDownloadService(final Activity context, UnsplashImage image) {
        Intent intent = new Intent(context, BackgroundDownloadService.class);
        intent.putExtra(Params.NAME_KEY, image.getFileNameForDownload());
        intent.putExtra(Params.URL_KEY, image.getDownloadUrl());
        context.startService(intent);

        ToastService.sendShortToast(context.getString(R.string.downloading_in_background));

        final DownloadItem item = new DownloadItem(image.getId(), image.getListUrl(), image.getDownloadUrl(),
                image.getFileNameForDownload());
        item.setColor(image.getThemeColor());
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(item);
            }
        });
    }
}
