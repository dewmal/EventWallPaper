package com.juniperphoton.myersplash.model;

import com.juniperphoton.myersplash.cloudservice.CloudService;

public class UnsplashCategory {
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

    public String getUrl() {
        if (id <= 0) {
            return CloudService.baseUrl;
        } else return links.self + "/";
    }

    public class links {
        private String self;
        private String photos;
    }
}
