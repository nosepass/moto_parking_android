package com.github.nosepass.motoparking.http;

import android.os.Build;

import com.github.nosepass.motoparking.MyUtil;

/**
 * Phone hardware info.
 */
public class PhoneInfo {
    public String deviceId;
    public String model;
    public String buildJson = "{}";

    public PhoneInfo(String deviceId) {
        this.deviceId = deviceId;
        this.model = Build.MODEL;
        this.buildJson = MyUtil.getBuildInfo().toString();
    }
}
