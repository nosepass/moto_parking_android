package com.github.nosepass.motoparking.http;

import org.json.JSONObject;

/**
 * The parameters to the login api endpoint.
 * Gson turns this into json that the api understands.
 */
public class LoginParameters {
    private static final String TAG = "http.Login";

    private Credentials credentials = new Credentials();
    private PhoneInfo phoneInfo = new PhoneInfo();

    public static class Credentials {
        public String nickname;
        public String password;
    }

    public static class PhoneInfo {
        public String deviceId;
        public String model;
        public JSONObject buildJson = new JSONObject();
    }

    public LoginParameters(String nickname, String password, String deviceId) {
        credentials.nickname = nickname;
        credentials.password = password;
        phoneInfo.deviceId = deviceId;
        phoneInfo.model = "xb12sx";
    }
}
