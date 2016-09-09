package com.juniperphoton.myersplash.model;

import retrofit2.http.PUT;

public class UnsplashImageFeatured {
    private UnsplashImage cover_photo;

    public UnsplashImageFeatured() {

    }

    public UnsplashImage getImage() {
        return cover_photo;
    }
}
