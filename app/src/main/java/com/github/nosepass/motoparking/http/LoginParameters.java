package com.github.nosepass.motoparking.http;

import java.util.Date;

/**
 * The parameters to the login api endpoint.
 * Gson turns this into json that the api understands.
 */
public class LoginParameters {
    private static final String TAG = "http.LoginParameters";

    Credentials credentials = new Credentials();
    PhoneInfo phoneInfo;
    Date createdAt;

    public static class Credentials {
        public String nickname;
        public String password;
    }

    public LoginParameters() { }

    public LoginParameters(String nickname, String password, String deviceId) {
        credentials.nickname = nickname;
        credentials.password = password;
        phoneInfo = new PhoneInfo(deviceId);
        createdAt = new Date();
    }
}
