package com.example.iuiutrash.models;

public class TeamMember {
    private String name;
    private String role;
    private String description;
    private int imageResource;

    public TeamMember(String name, String role, String description, int imageResource) {
        this.name = name;
        this.role = role;
        this.description = description;
        this.imageResource = imageResource;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResource() {
        return imageResource;
    }
} 