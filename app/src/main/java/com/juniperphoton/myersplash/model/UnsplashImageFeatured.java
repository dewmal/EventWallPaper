package com.juniperphoton.myersplash.model;

import com.google.gson.annotations.SerializedName;

public class UnsplashImageFeatured {
    @SerializedName("coverPhoto")
    private UnsplashImage mCoverPhoto;

    public UnsplashImageFeatured() {

    }

    public UnsplashImage getImage() {
        return mCoverPhoto;
    }
}
