package com.juniperphoton.myersplash.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DownloadItem extends RealmObject {

    public enum DownloadStatus {
        Downloading,
        Failed,
        Completed
    }

    private String mThumbUrl;

    private String mDownloadUrl;

    @PrimaryKey
    private String mId;

    private int mProgress;

    private int mColor;

    private int mStatus;

    private String mFilePath;

    public DownloadItem(String id, String thumbUrl, String downloadUrl) {
        mId = id;
        mThumbUrl = thumbUrl;
        mDownloadUrl = downloadUrl;
        mStatus = DownloadStatus.Downloading.ordinal();
    }

    public DownloadItem() {

    }

    public int getProgress() {
        return mProgress;
    }

    public void setStatus(DownloadStatus status) {
        mStatus = status.ordinal();
    }

    public int getStatus() {
        return mStatus;
    }

    public void setProgress(int progress) {
        mProgress = progress;
        if (mProgress >= 100) {
            mStatus = DownloadStatus.Completed.ordinal();
        }
    }

    public String getProgressStr() {
        if (mProgress >= 100) {
            return "";
        } else {
            switch (mStatus) {
                case 0:
                    return "DOWNLOADING";
                case 1:
                    return "FAILED";
                case 2:
                    return "COMPLETED";
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
