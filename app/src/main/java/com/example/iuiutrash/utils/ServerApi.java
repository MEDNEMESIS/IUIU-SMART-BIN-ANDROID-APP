package com.example.iuiutrash.utils;

import android.content.Context;
import android.util.Log;

import com.example.iuiutrash.model.BinDataModel;
import com.example.iuiutrash.model.BinModel;
import com.example.iuiutrash.model.User;
import com.example.iuiutrash.model.FeedbackModel;
import com.example.iuiutrash.utils.libs.HttpRequest;
import com.example.iuiutrash.utils.libs.HttpResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerApi extends CustomAlertDialog {
    private static final String TAG = "ServerApi";
    private final Context context;
    private final HttpRequest request;
    private String username;

    public interface Callback<T> {
        void onResults(HttpResult<T> result);
    }

    public ServerApi(Context context) {
        super(context);
        this.context = context;
        this.request = new HttpRequest(context, Constants.baseUrl);
        this.username = "";
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void login(Map<String, String> params, Callback callback) {
        Log.d(TAG, "Attempting login");
        request.postData("/login", params, User.class, (HttpRequest.ApiCallback<User>) result -> {
            Log.d(TAG, "Login response: " + result.toString());
            callback.onResults(result);
        });
    }

    public void bins(Map<String, String> params, Callback callback) {
        params.put("username", username);
        request.postData("/bins", params, BinModel.class, (HttpRequest.ApiCallback<BinModel>) result -> {
            if (!result.status) {
                Log.e(TAG, "Failed to fetch bins: " + result.message);
                showAlert("Connection Error", getErrorMessage(result.message));
            }
            callback.onResults(result);
        });
    }

    public void binData(Map<String, String> params, Callback callback) {
        Log.d(TAG, "Fetching bin data");
        params.put("username", username);
        request.postData("/binData", params, BinDataModel.class, (HttpRequest.ApiCallback<BinDataModel>) result -> {
            if (!result.status) {
                Log.e(TAG, "Failed to fetch bin data: " + result.message);
                showAlert("Connection Error", getErrorMessage(result.message));
            }
            callback.onResults(result);
        });
    }

    public void binStats(Map<String, String> params, Callback callback) {
        Log.d(TAG, "Fetching bin statistics");
        params.put("username", username);
        request.postData("/bins", params, BinModel.class, (HttpRequest.ApiCallback<BinModel>) result -> {
            if (!result.status) {
                Log.e(TAG, "Failed to fetch bin statistics: " + result.message);
                showAlert("Connection Error", getErrorMessage(result.message));
            }
            callback.onResults(result);
        });
    }

    public void updateProfile(Map<String, String> params, Callback callback) {
        Log.d(TAG, "Updating user profile");
        params.put("username", username);
        request.postData("/updateProfile.php", params, User.class, (HttpRequest.ApiCallback<User>) result -> {
            if (!result.status) {
                Log.e(TAG, "Failed to update profile: " + result.message);
                showAlert("Update Failed", getErrorMessage(result.message));
            }
            callback.onResults(result);
        });
    }

    public void signup(Map<String, String> params, Callback callback) {
        Log.d(TAG, "Attempting signup");
        request.postData("/signup", params, User.class, (HttpRequest.ApiCallback<User>) result -> {
            if (!result.status) {
                Log.e(TAG, "Signup failed: " + result.message);
                showAlert("Signup Failed", getErrorMessage(result.message));
            } else if (result.hasData()) {
                User user = (User) result.getValueAt(0);
                setUsername(user.getEmail());
            }
            callback.onResults(result);
        });
    }

    public void users(Map<String, String> params, Callback callback) {
        Log.d(TAG, "Making users API call with username: " + username);
        if (!params.containsKey("username")) {
            params.put("username", username);
        }
        request.postData("/users", params, User.class, (HttpRequest.ApiCallback<User>) result -> {
            if (!result.status) {
                Log.e(TAG, "Users API call failed: " + result.message);
                showAlert("Error", getErrorMessage(result.message));
            }
            callback.onResults(result);
        });
    }

    public void feedback(Map<String, String> params, Callback callback) {
        Log.d(TAG, "Submitting feedback with params: " + params);
        request.postData("/feedback", params, FeedbackModel.class, new HttpRequest.ApiCallback<FeedbackModel>() {
            @Override
            public void onResults(HttpResult<FeedbackModel> result) {
                Log.d(TAG, "Raw feedback response: " + result.toString());
                Log.d(TAG, "Response status: " + result.status);
                Log.d(TAG, "Response message: " + result.message);
                Log.d(TAG, "Response rows: " + result.rows());
                Log.d(TAG, "Response has data: " + result.hasData());
                
                if (result.hasData()) {
                    Log.d(TAG, "Response values: " + result.values);
                    for (int i = 0; i < result.rows(); i++) {
                        FeedbackModel feedback = result.getValueAt(i);
                        if (feedback != null) {
                            Log.d(TAG, "Feedback " + i + ": id=" + feedback.getId() + 
                                      ", username=" + feedback.getUsername() + 
                                      ", rating=" + feedback.getRating() + 
                                      ", text=" + feedback.getFeedbackText() + 
                                      ", fullname=" + feedback.getFullname());
                        } else {
                            Log.w(TAG, "Feedback at index " + i + " is null");
                        }
                    }
                }
                
                callback.onResults(result);
            }
        });
    }

    public void submitFeedback(String username, String feedbackText, float rating, Callback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("action", "new");
        params.put("feedback", feedbackText);
        params.put("rating", String.valueOf(rating));

        Log.d(TAG, "Submitting feedback with params: " + params);
        request.postData("/feedback", params, FeedbackModel.class, new HttpRequest.ApiCallback<FeedbackModel>() {
            @Override
            public void onResults(HttpResult<FeedbackModel> result) {
                callback.onResults(result);
            }
        });
    }

    public void deleteFeedback(String username, int feedbackId, Callback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("action", "delt");
        params.put("id", String.valueOf(feedbackId));

        Log.d(TAG, "Deleting feedback with params: " + params);
        request.postData("/feedback", params, FeedbackModel.class, new HttpRequest.ApiCallback<FeedbackModel>() {
            @Override
            public void onResults(HttpResult<FeedbackModel> result) {
                callback.onResults(result);
            }
        });
    }

    private String getErrorMessage(String error) {
        if (error == null) {
            return "An unknown error occurred";
        }
        
        if (error.contains("No internet connection")) {
            return "Please check your internet connection and try again";
        }
        
        if (error.contains("Failed to connect")) {
            return "Unable to connect to the server. Please try again later";
        }
        
        if (error.contains("timeout")) {
            return "The server is taking too long to respond. Please try again";
        }
        
        return "Error: " + error;
    }
}
