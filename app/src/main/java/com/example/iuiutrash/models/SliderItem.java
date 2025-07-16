package com.example.iuiutrash.models;

public class SliderItem {
    private int imageResource;
    private String title;

    public SliderItem(int imageResource, String title) {
        this.imageResource = imageResource;
        this.title = title;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getTitle() {
        return title;
    }
} 