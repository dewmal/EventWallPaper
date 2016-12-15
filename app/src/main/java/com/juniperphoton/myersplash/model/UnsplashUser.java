package com.juniperphoton.myersplash.model;

import java.io.Serializable;

public class UnsplashUser implements Serializable {
    private String id;
    private String username;
    private String name;
    private ProfileUrl links;

    public UnsplashUser() {

    }

    public String getName() {
        return name;
    }

    public String getHomeUrl() {
        return links.html;
    }

    public class ProfileUrl implements Serializable {
        private String self;
        private String html;
        private String photos;
        private String likes;
        private String portfolio;

        public ProfileUrl() {

        }
    }
}
