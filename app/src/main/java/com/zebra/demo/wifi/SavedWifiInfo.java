package com.zebra.demo.wifi;

public class SavedWifiInfo {
    String wifiName;
    String wifiPwd;
    int wifiLevel;
    boolean lock;
    public SavedWifiInfo() {

    }

    public SavedWifiInfo(String wifiName, String wifiPwd, int wifiLevel, boolean lock) {
        this.wifiName = wifiName;
        this.wifiPwd = wifiPwd;
        this.wifiLevel = wifiLevel;
        this.lock = lock;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public String getWifiPwd() {
        return wifiPwd;
    }

    public void setWifiPwd(String wifiPwd) {
        this.wifiPwd = wifiPwd;
    }

    public int getWifiLevel() {
        return wifiLevel;
    }

    public void setWifiLevel(int wifiLevel) {
        this.wifiLevel = wifiLevel;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }
}
