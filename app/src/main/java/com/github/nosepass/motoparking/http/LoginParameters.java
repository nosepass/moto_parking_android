package com.github.nosepass.motoparking.http;

import android.os.Build;

import com.github.nosepass.motoparking.MyLog;
import com.github.nosepass.motoparking.MyUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.Date;

/**
 * The parameters to the login api endpoint.
 * Gson turns this into json that the api understands.
 */
public class LoginParameters {
    private static final String TAG = "http.LoginParameters";

    private Credentials credentials = new Credentials();
    private PhoneInfo phoneInfo = new PhoneInfo();
    private Date createdAt;

    public static class Credentials {
        public String nickname;
        public String password;
    }

    public static class PhoneInfo {
        public String deviceId;
        public String model;
        public String buildJson = "{}";
    }

    public LoginParameters(String nickname, String password, String deviceId) {
        credentials.nickname = nickname;
        credentials.password = password;
        phoneInfo.deviceId = deviceId;
        phoneInfo.model = Build.MODEL;
        phoneInfo.buildJson = MyUtil.getBuildInfo().toString();
        createdAt = new Date();
    }
}
