package com.juniperphoton.myersplash.model;

import android.graphics.Color;

import com.juniperphoton.myersplash.base.App;
import com.juniperphoton.myersplash.common.Constant;
import com.juniperphoton.myersplash.utils.LocalSettingHelper;

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

    public String getId(){
        return id;
    }

    public String getListUrl() {
        final int choice = LocalSettingHelper.getInt(App.getInstance(), Constant.LOADING_QUALITY_CONFIG_NAME, 0);
        String url = null;
        switch (choice) {
            case 0:
                url = urls.regular;
                break;
            case 1:
                url = urls.small;
                break;
            case 2:
                url = urls.thumb;
                break;
        }
        return url;
    }

    public String getDownloadUrl() {
        final int choice = LocalSettingHelper.getInt(App.getInstance(), Constant.SAVING_QUALITY_CONFIG_NAME, 1);
        String url = null;
        switch (choice) {
            case 0:
                url = urls.raw;
                break;
            case 1:
                url = urls.full;
                break;
            case 2:
                url = urls.small;
                break;
        }
        return url + "/";
    }

    public String getFileNameForDownload() {
        return user.getName() + "-" + created_at + ".jpg";
    }

    public int getThemeColor() {
        return Color.parseColor(color);
    }

    public String getUserName() {
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
