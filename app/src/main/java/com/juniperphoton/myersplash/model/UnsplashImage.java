package com.juniperphoton.myersplash.model;

public class UnsplashImage {
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

    public class ImageUrl {
        private String raw;
        private String full;
        private String regular;
        private String small;
        private String thumb;

        public ImageUrl() {

        }
    }
}
