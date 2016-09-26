package com.juniperphoton.myersplash.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.support.v7.app.AlertDialog;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.base.App;
import com.juniperphoton.myersplash.service.BackgroundDownloadService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import okhttp3.ResponseBody;

public class DownloadUtil {
    private static String TAG = "DownloadUtil";

    public static File writeResponseBodyToDisk(ResponseBody body, String expectedName, final String url) {
        try {
            File folder = new File(getGalleryPath());
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File fileToSave = new File(folder + File.separator + expectedName);

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
                        NotificationUtil.showProgressNotification("MyerSplash", "Downloading...", progressToReport, Uri.parse(url));
                    }
                    Log.d(TAG, "progress: " + progress + ",last:" + progressToReport);
                }
                long endTime = new Date().getTime();

                Log.d(TAG, "time spend=" + String.valueOf(endTime - startTime));

                outputStream.flush();

                NotificationUtil.showCompleteNotification(Uri.parse(url), Uri.fromFile(fileToSave));

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

    /**
     * 获得媒体库的文件夹
     *
     * @return 路径
     */
    public static String getGalleryPath() {
        File mediaStorageDir = null;
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

    public static void checkAndDownload(final Activity context, final String fileName, final String url) {
        RequestUtil.checkAndRequest(context);
        if(!RequestUtil.check(context)){
            return;
        }
        if (!NetworkUtil.usingWifi(context)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
            builder.setTitle("ATTENTION");
            builder.setMessage("You are not using WiFi network. Continue to download?");
            builder.setPositiveButton("DOWNLOAD", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startDownloadService(context, fileName, url);
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } else {
            startDownloadService(context, fileName, url);
        }
    }

    private static void startDownloadService(final Activity context, final String fileName, final String url) {
        String fixedUrl = fixUri(url);

        Intent intent = new Intent(context, BackgroundDownloadService.class);
        intent.putExtra("NAME", fileName);
        intent.putExtra("URI", fixedUrl);
        context.startService(intent);
        ToastService.sendShortToast("Downloading...");
    }

    private static String fixUri(String url) {
        String outputUrl = url;
        if (outputUrl.endsWith("/")) {
            outputUrl = outputUrl.substring(0, outputUrl.length() - 1);
        }
        return outputUrl;
    }
}
