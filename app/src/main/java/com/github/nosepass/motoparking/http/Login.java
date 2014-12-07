package com.github.nosepass.motoparking.http;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.github.nosepass.motoparking.MotoParkingApplication;
import com.github.nosepass.motoparking.MyLog;
import com.github.nosepass.motoparking.PrefKeys;
import com.google.gson.JsonObject;

/**
 * Hit the login url to establish a session on the server
 */
public class Login extends HttpAction {
    private static final String TAG = "http.Login";
    SharedPreferences prefs;
    LoginParameters params;
    UserResponse response;

    public Login(SharedPreferences prefs, TelephonyManager tm) {
        this.prefs = prefs;
        String deviceId = tm.getDeviceId();
        String nickname = prefs.getString(PrefKeys.NICKNAME, "");
        String pw = prefs.getString(PrefKeys.PASSWORD, "");
        params = new LoginParameters(nickname, pw, deviceId);
    }

    public Login(SharedPreferences prefs, JsonObject json) {
        this.prefs = prefs;
        this.params = gson.fromJson(json, LoginParameters.class);
    }

    public void executeHttpRequest() {
        MyLog.v(TAG, "downloading stuff");
        LoginApi api = MotoParkingApplication.loginApi;
        response = api.login(params);
        MyLog.v(TAG, "" + response);
    }

    @Override
    public void processResponse(Context c) {
        if (response != null) {
            saveNewUserInfoIfNecessary(response);
        }
    }

    @Override
    public boolean isLoginAction() {
        return true;
    }

    // don't log the password with the superclass toString()
    public String toString() {
        return getClass().getSimpleName();
    }

    public String toJson() {
        return toJson(params);
    }

    private void saveNewUserInfoIfNecessary(UserResponse result) {
        if (result.password != null) {
            // password is only populated if an anonymous user was just created
            prefs.edit()
                    .putString(PrefKeys.NICKNAME, result.nickname)
                    .putString(PrefKeys.PASSWORD, result.password)
                    .apply();
        }
    }
}
