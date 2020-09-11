package com.example.android.bluetoothadvertisements;

import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanSettings;

public class AppSettings {
    private AppSettings() {}

    private int scanPeriodSeconds = 0;
    private int scanMode = ScanSettings.SCAN_MODE_LOW_LATENCY;
    private int advertiseMode = AdvertiseSettings.ADVERTISE_MODE_LOW_POWER;
    private int advertisePower = AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM;
    private int advertiseTerminationDelayMs = 0;

    public OnScannerSettingsChanged scannerChangeListener = null;
    public OnAdvertiserSettingsChanged advertiserChangeListener = null;

    public int getScanPeriodSeconds() {
        return scanPeriodSeconds;
    }

    public void setScanPeriodSeconds(int scanPeriodSeconds) {
        int prev = this.scanPeriodSeconds;
        this.scanPeriodSeconds = scanPeriodSeconds;
        if (prev != scanPeriodSeconds && null != scannerChangeListener) {
            scannerChangeListener.onChanged();
        }
    }

    public int getScanMode() {
        return scanMode;
    }

    public void setScanMode(int scanMode) {
        int prev = this.scanMode;
        this.scanMode = scanMode;
        if (prev != scanMode && null != scannerChangeListener) {
            scannerChangeListener.onChanged();
        }
    }

    public int getAdvertiseMode() {
        return advertiseMode;
    }

    public void setAdvertiseMode(int advertiseMode) {
        int prev = this.advertiseMode;
        this.advertiseMode = advertiseMode;
        if (prev != advertiseMode && null != advertiserChangeListener) {
            advertiserChangeListener.onChanged();
        }
    }

    public int getAdvertisePower() {
        return advertisePower;
    }

    public void setAdvertisePower(int advertisePower) {
        int prev = this.advertisePower;
        this.advertisePower = advertisePower;
        if (prev != advertisePower && null != advertiserChangeListener) {
            advertiserChangeListener.onChanged();
        }
    }

    public int getAdvertiseTerminationDelayMs() {
        return advertiseTerminationDelayMs;
    }

    public void setAdvertiseTerminationDelayMs(int advertiseTimeout) {
        int prev = this.advertiseTerminationDelayMs;
        this.advertiseTerminationDelayMs = advertiseTimeout;
        if (prev != advertiseTimeout && null != advertiserChangeListener) {
            advertiserChangeListener.onChanged();
        };
    }


    static AppSettings getInstance() {
        return instance;
    }
    private final static AppSettings instance = new AppSettings();

    interface OnScannerSettingsChanged {
        void onChanged();
    }

    interface OnAdvertiserSettingsChanged {
        void onChanged();
    }
}
