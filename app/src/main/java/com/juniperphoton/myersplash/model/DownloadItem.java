package com.juniperphoton.myersplash.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DownloadItem extends RealmObject {

    private String mThumbUrl;

    private String mDownloadUrl;

    @PrimaryKey
    private String mId;

    private int mProgress;

    private int mColor;

    public DownloadItem(String id, String thumbUrl, String downloadUrl) {
        mId = id;
        mThumbUrl = thumbUrl;
        mDownloadUrl = downloadUrl;
    }

    public DownloadItem() {

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
}
