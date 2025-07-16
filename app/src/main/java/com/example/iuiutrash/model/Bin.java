package com.example.iuiutrash.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

public class Bin implements Parcelable {
    private int binid;
    private String bincode;
    private int currentlevel;
    private String binstatus;
    private String location;

    public Bin() {
        this.bincode = "";
        this.currentlevel = 0;
        this.binstatus = "active";
        this.location = "";
    }

    public Bin(String bincode, int currentlevel, String binstatus, String location) {
        this.bincode = bincode;
        this.currentlevel = currentlevel;
        this.binstatus = binstatus;
        this.location = location;
    }

    protected Bin(Parcel in) {
        binid = in.readInt();
        bincode = in.readString();
        currentlevel = in.readInt();
        binstatus = in.readString();
        location = in.readString();
    }

    public static final Creator<Bin> CREATOR = new Creator<Bin>() {
        @Override
        public Bin createFromParcel(Parcel in) {
            return new Bin(in);
        }

        @Override
        public Bin[] newArray(int size) {
            return new Bin[size];
        }
    };

    public int getBinId() {
        return binid;
    }

    public void setBinId(int binid) {
        this.binid = binid;
    }

    public String getBinCode() {
        return bincode;
    }

    public void setBinCode(String bincode) {
        this.bincode = bincode;
    }

    public int getCurrentLevel() {
        return currentlevel;
    }

    public void setCurrentLevel(int currentlevel) {
        this.currentlevel = currentlevel;
    }

    public String getBinStatus() {
        return binstatus;
    }

    public void setBinStatus(String binstatus) {
        this.binstatus = binstatus;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(binid);
        dest.writeString(bincode);
        dest.writeInt(currentlevel);
        dest.writeString(binstatus);
        dest.writeString(location);
    }

    @Override
    public String toString() {
        return "Bin{" +
                "binid=" + binid +
                ", bincode='" + bincode + '\'' +
                ", currentlevel=" + currentlevel +
                ", binstatus='" + binstatus + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
} 