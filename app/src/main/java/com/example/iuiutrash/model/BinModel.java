package com.example.iuiutrash.model;


public class BinModel {
    private int binid;
    private int currentlevel;
    private String bincode;
    private String binstatus;
    private String location;


    public int getBinid() {
        return binid;
    }

    public String getBincode() {
        return bincode;
    }

    public String getStatus() {
        return binstatus;
    }

    public String getLocation() {
        return location;
    }

    public String getBinStatus() {
        return binstatus;
    }

    public int getCurrentLevel() {
        return currentlevel;
    }

    public void setBinStatus(String value) {
        this.binstatus = value;
    }

    public void setCurrentLevel(int value) {
        this.currentlevel = value;
    }
}
