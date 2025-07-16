package com.example.iuiutrash.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.iuiutrash.model.User;
import com.google.gson.Gson;

public class UserManager {
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USER = "user";
    private static UserManager instance;
    private final SharedPreferences preferences;
    private final Gson gson;
    private User currentUser;

    private UserManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadUser();
    }

    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context.getApplicationContext());
        }
        return instance;
    }

    private void loadUser() {
        String userJson = preferences.getString(KEY_USER, null);
        if (userJson != null) {
            currentUser = gson.fromJson(userJson, User.class);
        }
    }

    public void saveUser(User user) {
        currentUser = user;
        String userJson = gson.toJson(user);
        preferences.edit().putString(KEY_USER, userJson).apply();
    }

    public User getUser() {
        return currentUser;
    }

    public void clearUser() {
        currentUser = null;
        preferences.edit().remove(KEY_USER).apply();
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
} 