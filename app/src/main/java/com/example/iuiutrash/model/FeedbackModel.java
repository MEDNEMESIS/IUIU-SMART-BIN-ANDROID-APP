package com.example.iuiutrash.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class FeedbackModel implements Parcelable {
    private static final String TAG = "FeedbackModel";
    private int id;
    private String username;
    private String fullname;
    private float rating;
    private String feedbackText;
    private String createdAt;

    public FeedbackModel() {
        // Default constructor
    }

    public FeedbackModel(JSONObject json) {
        try {
            Log.d(TAG, "Parsing feedback JSON: " + json.toString());
            
            if (json.has("id")) {
                this.id = json.getInt("id");
                Log.d(TAG, "Parsed id: " + this.id);
            }
            
            if (json.has("username")) {
                this.username = json.getString("username");
                Log.d(TAG, "Parsed username: " + this.username);
            }
            
            if (json.has("fullname")) {
                this.fullname = json.getString("fullname");
                Log.d(TAG, "Parsed fullname: " + this.fullname);
            }
            
            if (json.has("rating")) {
                Object ratingObj = json.get("rating");
                if (ratingObj instanceof Number) {
                    this.rating = ((Number) ratingObj).floatValue();
                } else if (ratingObj instanceof String) {
                    try {
                        this.rating = Float.parseFloat((String) ratingObj);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing rating string: " + ratingObj);
                        this.rating = 0.0f;
                    }
                } else {
                    Log.e(TAG, "Unexpected rating type: " + ratingObj.getClass().getName());
                    this.rating = 0.0f;
                }
                Log.d(TAG, "Parsed rating: " + this.rating);
            }
            
            if (json.has("feedback_text")) {
                this.feedbackText = json.getString("feedback_text");
                Log.d(TAG, "Parsed feedback_text: " + this.feedbackText);
            }
            
            if (json.has("created_at")) {
                this.createdAt = json.getString("created_at");
                Log.d(TAG, "Parsed created_at: " + this.createdAt);
            }
            
            Log.d(TAG, "Successfully created FeedbackModel: " + this.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
            // Initialize with default values
            this.id = 0;
            this.username = "";
            this.fullname = "";
            this.rating = 0.0f;
            this.feedbackText = "";
            this.createdAt = "";
        }
    }

    protected FeedbackModel(Parcel in) {
        id = in.readInt();
        username = in.readString();
        fullname = in.readString();
        rating = in.readFloat();
        feedbackText = in.readString();
        createdAt = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(username);
        dest.writeString(fullname);
        dest.writeFloat(rating);
        dest.writeString(feedbackText);
        dest.writeString(createdAt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FeedbackModel> CREATOR = new Creator<FeedbackModel>() {
        @Override
        public FeedbackModel createFromParcel(Parcel in) {
            return new FeedbackModel(in);
        }

        @Override
        public FeedbackModel[] newArray(int size) {
            return new FeedbackModel[size];
        }
    };

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getFullname() { return fullname; }
    public float getRating() { return rating; }
    public String getFeedbackText() { return feedbackText; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    public void setRating(float rating) { this.rating = rating; }
    public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "FeedbackModel{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullname='" + fullname + '\'' +
                ", rating=" + rating +
                ", feedbackText='" + feedbackText + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
} 