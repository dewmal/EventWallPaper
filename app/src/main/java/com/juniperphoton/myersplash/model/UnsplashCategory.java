package com.juniperphoton.myersplash.model;

import com.juniperphoton.myersplash.cloudservice.CloudService;

public class UnsplashCategory {
    public final static int FEATURED_CATEGORY_ID = 10000;
    public final static int NEW_CATEGORY_ID = 10001;
    public final static int RANDOM_CATEOGORY_ID = 10002;
    public final static String FEATURE_S = "Featured";
    public final static String NEW_S = "New";
    public final static String RANDOM_S = "Random";

    private int id;
    private String title;
    private int photo_count;
    private links links;

    public UnsplashCategory() {

    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public String getRequestUrl() {
        if (id == NEW_CATEGORY_ID) {
            return CloudService.PHOTO_URL;
        } else if (id == FEATURED_CATEGORY_ID) {
            return CloudService.FEATURED_PHOTO_URL;
        } else if (id == RANDOM_CATEOGORY_ID) {
            return CloudService.RANDOM_PHOTOS_URL;
        } else return links.photos;
    }

    public String getWebsiteUrl() {
        return links.html;
    }

    public class links {
        private String self;
        private String photos;
        private String html;
    }
}
