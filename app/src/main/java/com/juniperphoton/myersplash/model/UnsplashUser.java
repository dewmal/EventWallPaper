package com.juniperphoton.myersplash.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UnsplashUser implements Serializable {
    @SerializedName("id")
    private String mId;

    @SerializedName("username")
    private String mUserName;

    @SerializedName("name")
    private String mName;

    @SerializedName("links")
    private ProfileUrl mLinks;

    public UnsplashUser() {

    }

    public String getName() {
        return mName;
    }

    public String getHomeUrl() {
        return mLinks.mHtml;
    }

    public static class ProfileUrl implements Serializable {
        @SerializedName("self")
        private String mSelf;

        @SerializedName("html")
        private String mHtml;

        @SerializedName("photos")
        private String mPhotos;

        @SerializedName("likes")
        private String mLikes;

        @SerializedName("portfolio")
        private String mPortfolio;

        public ProfileUrl() {

        }
    }
}
