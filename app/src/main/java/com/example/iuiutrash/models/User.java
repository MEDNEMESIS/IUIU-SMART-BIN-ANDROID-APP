package com.example.iuiutrash.models;

public class User {
    public String email;
    public String password;
    public String fullName;
    private byte[] profilePhoto;

    public User(String email, String password, String fullName) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    public User(String email, String password, String fullName, byte[] profilePhoto) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.profilePhoto = profilePhoto;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public byte[] getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(byte[] profilePhoto) {
        this.profilePhoto = profilePhoto;
    }
} 