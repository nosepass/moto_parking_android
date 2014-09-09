package com.github.nosepass.motoparking.http;

import android.os.Build;

import com.github.nosepass.motoparking.MyLog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Extract phone model information for logging
 */
public class PhoneInfoBuilder {
    private static final String TAG = "http.PhoneInfoBuilder";

    public JSONObject buildInfo(String deviceId) {
        try {
            JSONObject info = new JSONObject();
            info.put("device_id", deviceId);
            String model = Build.BRAND + " " + Build.MODEL;
            info.put("model", model);
            JSONObject allBuildInfo = new JSONObject();
            allBuildInfo.put("ID", Build.ID);
            allBuildInfo.put("DISPLAY", Build.DISPLAY);
            allBuildInfo.put("PRODUCT", Build.PRODUCT);
            allBuildInfo.put("DEVICE", Build.DEVICE);
            allBuildInfo.put("BOARD", Build.BOARD);
            allBuildInfo.put("CPU_ABI", Build.CPU_ABI);
            allBuildInfo.put("CPU_ABI2", Build.CPU_ABI2);
            allBuildInfo.put("MANUFACTURER", Build.MANUFACTURER);
            allBuildInfo.put("BRAND", Build.BRAND);
            allBuildInfo.put("MODEL", Build.MODEL);
            allBuildInfo.put("BOOTLOADER", Build.BOOTLOADER);
            allBuildInfo.put("RADIO", Build.getRadioVersion());
            allBuildInfo.put("HARDWARE", Build.HARDWARE);
            allBuildInfo.put("SERIAL", Build.SERIAL);

            JSONObject versionInfo = new JSONObject();
            versionInfo.put("INCREMENTAL", Build.VERSION.INCREMENTAL);
            versionInfo.put("RELEASE", Build.VERSION.RELEASE);
            versionInfo.put("SDK", Build.VERSION.SDK);
            versionInfo.put("SDK_INT", Build.VERSION.SDK_INT);
            versionInfo.put("CODENAME", Build.VERSION.CODENAME);
            allBuildInfo.put("VERSION", versionInfo);

            allBuildInfo.put("TYPE", Build.TYPE);
            allBuildInfo.put("TAGS", Build.TAGS);
            allBuildInfo.put("FINGERPRINT", Build.FINGERPRINT);
            allBuildInfo.put("TIME", Build.TIME);
            allBuildInfo.put("USER", Build.USER);
            allBuildInfo.put("HOST", Build.HOST);

            info.put("build_json", allBuildInfo);

            return info;
        } catch (JSONException e) {
            MyLog.e(TAG, e);
        }
        return new JSONObject();
    }
}
