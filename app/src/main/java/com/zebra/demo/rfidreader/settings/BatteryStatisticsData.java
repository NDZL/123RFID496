package com.zebra.demo.rfidreader.settings;

import java.util.List;

public class BatteryStatisticsData {
    private String batteryHeader;
    private List<String> batteryItems;
    private List<String> batteryItemData;

    public BatteryStatisticsData(String batteryHeader, List<String> batteryItems, List<String> batteryItemData) {
        this.batteryHeader = batteryHeader;
        this.batteryItems = batteryItems;
        this.batteryItemData = batteryItemData;
    }

    public String getBatteryHeader() {
        return batteryHeader;
    }

    public void setBatteryHeader(String batteryHeader) {
        this.batteryHeader = batteryHeader;
    }

    public List<String> getBatteryItems() {
        return batteryItems;
    }

    public void setBatteryItems(List<String> batteryItems) {
        this.batteryItems = batteryItems;
    }

    public List<String> getBatteryItemData() {
        return batteryItemData;
    }

    public void setBatteryItemData(List<String> batteryItemData) {
        this.batteryItemData = batteryItemData;
    }
}
