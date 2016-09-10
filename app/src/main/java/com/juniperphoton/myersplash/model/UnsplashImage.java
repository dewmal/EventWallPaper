package com.juniperphoton.myersplash.model;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class UnsplashImage implements Serializable {
    private String id;
    private String created_at;
    private String color;
    private int likes;
    private UnsplashUser user;
    private ImageUrl urls;

    public UnsplashImage() {

    }

    public String getListUrl() {
        return urls.regular;
    }

    public String getDownloadUrl() {
        return urls.full + "/";
    }

    public String getFileNameForDownload() {
        return user.getName() + "-" + created_at + ".jpg";
    }

    public int getThemeColor(){
        return Color.parseColor(color);
    }

    public String getUserName(){
        return user.getName();
    }

    public class ImageUrl implements Serializable {
        private String raw;
        private String full;
        private String regular;
        private String small;
        private String thumb;

        public ImageUrl() {

        }
    }
}
