package com.juniperphoton.myersplash.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.google.gson.annotations.SerializedName;
import com.juniperphoton.myersplash.base.App;
import com.juniperphoton.myersplash.common.Constant;
import com.juniperphoton.myersplash.utils.DownloadUtil;
import com.juniperphoton.myersplash.utils.LocalSettingHelper;

import java.io.File;
import java.io.Serializable;

public class UnsplashImage implements Serializable {
    @SerializedName("id")
    private String mId;

    @SerializedName("created_at")
    private String mCreatedAt;

    @SerializedName("color")
    private String mColor;

    @SerializedName("likes")
    private int mLikes;

    @SerializedName("user")
    private UnsplashUser mUser;

    @SerializedName("urls")
    private ImageUrl mUrls;

    public UnsplashImage() {

    }

    public String getId() {
        return mId;
    }

    public String getListUrl() {
        final int choice = LocalSettingHelper.getInt(App.getInstance(), Constant.LOADING_QUALITY_CONFIG_NAME, 0);
        String url = null;
        if (mUrls == null) {
            return null;
        }
        switch (choice) {
            case 0:
                url = mUrls.mRegular;
                break;
            case 1:
                url = mUrls.mSmall;
                break;
            case 2:
                url = mUrls.mThumb;
                break;
        }
        return url;
    }

    public String getDownloadUrl() {
        final int choice = LocalSettingHelper.getInt(App.getInstance(), Constant.SAVING_QUALITY_CONFIG_NAME, 1);
        String url = null;
        switch (choice) {
            case 0:
                url = mUrls.mRaw;
                break;
            case 1:
                url = mUrls.mFull;
                break;
            case 2:
                url = mUrls.mSmall;
                break;
        }
        return url;
    }

    private String getTagForDownloadUrl() {
        final int choice = LocalSettingHelper.getInt(App.getInstance(), Constant.SAVING_QUALITY_CONFIG_NAME, 1);
        String tag = "";
        switch (choice) {
            case 0:
                tag = "raw";
                break;
            case 1:
                tag = "regular";
                break;
            case 2:
                tag = "small";
                break;
        }
        return tag;
    }

    public String getPathForDownload() {
        return DownloadUtil.getGalleryPath() + File.separator + getFileNameForDownload();
    }

    public boolean hasDownloaded() {
        String path = getPathForDownload();
        File file = new File(path);
        boolean exist = file.exists();
        if (exist) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 64;
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                boolean valid = bitmap != null;
                if (bitmap != null) {
                    bitmap.recycle();
                }
                return valid;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public String getFileNameForDownload() {
        return mUser.getName() + "-" + mId + "-" + getTagForDownloadUrl() + ".jpg";
    }

    public int getThemeColor() {
        return Color.parseColor(mColor);
    }

    public String getUserName() {
        return mUser.getName();
    }

    public String getUserHomePage() {
        return mUser.getHomeUrl();
    }

    private static class ImageUrl implements Serializable {
        @SerializedName("raw")
        private String mRaw;

        @SerializedName("full")
        private String mFull;

        @SerializedName("regular")
        private String mRegular;

        @SerializedName("small")
        private String mSmall;

        @SerializedName("thumb")
        private String mThumb;

        public ImageUrl() {

        }
    }
}
