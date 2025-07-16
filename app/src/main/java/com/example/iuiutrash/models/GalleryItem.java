package com.example.iuiutrash.models;

public class GalleryItem {
    private int imageResId;
    private String title;
    private String description;

    public GalleryItem(int imageResId, String title, String description) {
        this.imageResId = imageResId;
        this.title = title;
        this.description = description;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
} 