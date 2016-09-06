package com.juniperphoton.myersplash.model;

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

    public class links {
        private String self;
        private String photos;
    }
}
