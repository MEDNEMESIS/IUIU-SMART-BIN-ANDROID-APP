package com.example.iuiutrash.model;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONObject;
import android.util.Log;

import androidx.annotation.NonNull;

public class User implements Parcelable {
    private static final String TAG = "User";
    private int usid;
    private String email;
    private String password;
    private String userrole;
    private String fullname;

    public User() {
        this.email = "";
        this.password = "";
        this.fullname = "";
        this.userrole = "collector";
    }

    public User(String email, String password, String fullname) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.userrole = "collector";
    }

    public User(String email, String password, String fullname, String userrole) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.userrole = userrole;
    }

    public User(int usid, String email, String fullname, String userrole) {
        this.usid = usid;
        this.email = email;
        this.fullname = fullname;
        this.userrole = userrole;
    }

    public User(int usid, String email, String fullname) {
        this.usid = usid;
        this.email = email;
        this.fullname = fullname;
        this.userrole = "collector";
    }

    public User(Object data) {
        try {
            if (data instanceof JSONObject) {
                JSONObject json = (JSONObject) data;
                this.usid = json.getInt("usid");
                this.email = json.getString("email");
                this.password = json.getString("password");
                this.userrole = json.getString("userrole");
                this.fullname = json.getString("fullname");
            } else if (data instanceof String) {
                JSONObject json = new JSONObject((String) data);
                this.usid = json.getInt("usid");
                this.email = json.getString("email");
                this.password = json.getString("password");
                this.userrole = json.getString("userrole");
                this.fullname = json.getString("fullname");
            }
            Log.d(TAG, "User created - Email: " + email + ", Role: " + userrole);
        } catch (Exception e) {
            Log.e(TAG, "Error creating user: " + e.getMessage());
        }
    }

    public int getUsid() { return usid; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getUserrole() { return userrole; }
    public String getFullname() { return fullname; }

    public void setId(int id) {
        this.usid = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFullName(String fullname) {
        this.fullname = fullname;
    }

    public void setUserRole(String userrole) {
        this.userrole = userrole;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(userrole);
    }

    public boolean isCollector() {
        return "collector".equalsIgnoreCase(userrole) || isAdmin();
    }

    public boolean canManageBins() {
        return isAdmin();
    }

    public boolean canViewBins() {
        return isAdmin() || isCollector();
    }

    public boolean canUpdateBinData() {
        return isAdmin() || isCollector();
    }

    public boolean canManageUsers() {
        return isAdmin();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(usid);
        dest.writeString(email);
        dest.writeString(userrole);
        dest.writeString(fullname);
        dest.writeString(password);
    }

    @Override
    public String toString() {
        return "User{" +
                "usid=" + usid +
                ", email='" + email + '\'' +
                ", userrole='" + userrole + '\'' +
                ", fullname='" + fullname + '\'' +
                '}';
    }
}