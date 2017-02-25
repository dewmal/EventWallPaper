package com.juniperphoton.myersplash.model;

import com.juniperphoton.myersplash.cloudservice.CloudService;

public class UnsplashCategory {
    public final static int FEATURED_CATEGORY_ID = 10000;
    public final static int NEW_CATEGORY_ID = 10001;
    public final static int RANDOM_CATEGORY_ID = 10002;
    public final static int SEARCH_ID = 10003;

    public final static String FEATURE_S = "Featured";
    public final static String NEW_S = "New";
    public final static String RANDOM_S = "Random";

    private int id;
    private String title;
    private int photo_count;
    private links links;

    public UnsplashCategory() {

    }

    public static UnsplashCategory getSearchCategory(String title) {
        UnsplashCategory category = new UnsplashCategory();
        category.setId(UnsplashCategory.SEARCH_ID);
        category.setTitle(title);

        return category;
    }

    public static UnsplashCategory getFeaturedCategory() {
        UnsplashCategory featureCategory = new UnsplashCategory();
        featureCategory.setId(UnsplashCategory.FEATURED_CATEGORY_ID);
        featureCategory.setTitle(UnsplashCategory.FEATURE_S);

        return featureCategory;
    }

    public static UnsplashCategory getNewCategory() {
        UnsplashCategory newCategory = new UnsplashCategory();
        newCategory.setId(UnsplashCategory.NEW_CATEGORY_ID);
        newCategory.setTitle(UnsplashCategory.NEW_S);

        return newCategory;
    }

    public static UnsplashCategory getRandomCategory() {
        UnsplashCategory randomCategory = new UnsplashCategory();
        randomCategory.setId(UnsplashCategory.RANDOM_CATEGORY_ID);
        randomCategory.setTitle(UnsplashCategory.RANDOM_S);

        return randomCategory;
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
        switch (id) {
            case NEW_CATEGORY_ID:
                return CloudService.PHOTO_URL;
            case FEATURED_CATEGORY_ID:
                return CloudService.FEATURED_PHOTO_URL;
            case RANDOM_CATEGORY_ID:
                return CloudService.RANDOM_PHOTOS_URL;
            case SEARCH_ID:
                return CloudService.SEARCH_URL;
            default:
                return links.photos;
        }
    }

    public String getWebsiteUrl() {
        return links.html;
    }

    public static class links {
        private String self;
        private String photos;
        private String html;
    }
}
