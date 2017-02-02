package com.juniperphoton.myersplash.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.base.App;
import com.juniperphoton.myersplash.model.DownloadItem;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.service.BackgroundDownloadService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
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

                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                int progressToReport = 0;

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

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
                    //Log.d(TAG, "progress: " + progress + ",last:" + progressToReport);
                }
                long endTime = new Date().getTime();

                Log.d(TAG, "time spend=" + String.valueOf(endTime - startTime));

                outputStream.flush();

                //NotificationUtil.showCompleteNotification(Uri.parse(url), Uri.fromFile(fileToSave));

                return fileToSave;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
                new SingleMediaScanner(App.getInstance(), fileToSave);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File getFileToSave(String expectedName) {
        File folder = new File(getGalleryPath());
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File fileToSave = new File(folder + File.separator + expectedName);
        return fileToSave;
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
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
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
        }
    }

    private static void startDownloadService(final Activity context, UnsplashImage image) {
        String fixedUrl = fixUri(image.getDownloadUrl());

        Intent intent = new Intent(context, BackgroundDownloadService.class);
        intent.putExtra(Params.NAME_KEY, image.getFileNameForDownload());
        intent.putExtra(Params.URL_KEY, fixedUrl);
        context.startService(intent);
        ToastService.sendShortToast(context.getString(R.string.downloading_in_background));

        final DownloadItem item = new DownloadItem(image.getId(), image.getListUrl(), fixedUrl,
                image.getFileNameForDownload());
        item.setColor(image.getThemeColor());
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(item);
            }
        });
    }

    private static String fixUri(String url) {
        String outputUrl = url;
        if (outputUrl.endsWith("/")) {
            outputUrl = outputUrl.substring(0, outputUrl.length() - 1);
        }
        return outputUrl;
    }
}
