package com.juniperphoton.myersplash.utils;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.juniperphoton.myersplash.base.App;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

public class DownloadUtil {
    private static String TAG = "DownloadUtil";

    public static boolean writeResponseBodyToDisk(ResponseBody body, String expectedName) {
        try {
            File folder = new File(getGalleryPath());
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File fileToSave = new File(folder + File.separator + expectedName);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(fileToSave);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (Exception e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
                new SingleMediaScanner(App.getInstance(), fileToSave);
                NotificationUitl.sent("MyerSplash", "Saved:D");

            }
        } catch (IOException e) {
            return false;
        }
    }

    private static String getGalleryPath() {
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
}
