package com.juniperphoton.myersplash.model;

import android.support.annotation.IntDef;
import android.view.PixelCopy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DownloadItem extends RealmObject {
    @IntDef({DOWNLOAD_STATUS_DOWNLOADING, DOWNLOAD_STATUS_OK, DOWNLOAD_STATUS_FAILED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DownloadStatus {
    }

    public static final int DOWNLOAD_STATUS_DOWNLOADING = 0;
    public static final int DOWNLOAD_STATUS_FAILED = 1;
    public static final int DOWNLOAD_STATUS_OK = 2;

    public static final String ID_KEY = "mId";
    public static final String DOWNLOAD_URL = "mDownloadUrl";

    private String mThumbUrl;

    private String mDownloadUrl;

    @PrimaryKey
    private String mId;

    private int mProgress;

    private int mColor;

    private int mStatus;

    private String mFilePath;

    private String mFileName;

    public DownloadItem(String id, String thumbUrl, String downloadUrl, String fileName) {
        mId = id;
        mThumbUrl = thumbUrl;
        mDownloadUrl = downloadUrl;
        mStatus = DOWNLOAD_STATUS_DOWNLOADING;
        mFileName = fileName;
    }

    public DownloadItem() {

    }

    public String getFileName() {
        return mFileName;
    }

    public int getProgress() {
        return mProgress;
    }

    public void setStatus(@DownloadStatus int status) {
        mStatus = status;
    }

    @DownloadStatus
    public int getStatus() {
        return mStatus;
    }

    public void setProgress(int progress) {
        mProgress = progress;
        if (mProgress >= 100) {
            mStatus = DOWNLOAD_STATUS_OK;
        }
    }

    public String getProgressStr() {
        if (mProgress >= 100) {
            return "";
        } else {
            switch (mStatus) {
                case DOWNLOAD_STATUS_DOWNLOADING:
                    return "DOWNLOADING";
                default:
                    return "";
            }
        }
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public int getColor() {
        return mColor;
    }

    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public String getThumbUrl() {
        return mThumbUrl;
    }

    public String getId() {
        return mId;
    }
}
