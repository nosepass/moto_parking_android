package com.github.nosepass.motoparking.http;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.github.nosepass.motoparking.MotoParkingApplication;
import com.github.nosepass.motoparking.MyLog;
import com.github.nosepass.motoparking.PrefKeys;
import com.google.gson.JsonObject;

import retrofit.RetrofitError;

/**
 * Hit the login url to establish a session on the server
 */
public class Login extends HttpAction {
    private static final String TAG = "http.Login";
    private static final String CLSNAME = Login.class.getName();
    public static final String COMPLETE_INTENT = CLSNAME + ".COMPLETE";
    public static final String FAIL_INTENT = CLSNAME + FAIL_SFX;
    public static final String EXTRA_INVALID_CREDENTIALS = CLSNAME + ".EXTRA_INVALID_CREDENTIALS";
    public static final String EXTRA_USER_CREATED = CLSNAME + ".EXTRA_USER_CREATED";

    LoginParameters params;
    User response;

    public Login(SharedPreferences prefs, TelephonyManager tm) {
        String deviceId = tm.getDeviceId();
        String nickname = prefs.getString(PrefKeys.NICKNAME, "");
        String pw = prefs.getString(PrefKeys.PASSWORD, "");
        params = new LoginParameters(nickname, pw, deviceId);
    }

    public Login(String nickname, SharedPreferences prefs, TelephonyManager tm) {
        String deviceId = tm.getDeviceId();
        String pw = prefs.getString(PrefKeys.PASSWORD, "");
        params = new LoginParameters(nickname, pw, deviceId);
    }

    public Login(JsonObject json) {
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
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            boolean isNew = saveNewUserInfoIfNecessary(prefs, response);
            Intent i = new Intent(COMPLETE_INTENT);
            if (isNew) {
                i.putExtra(EXTRA_USER_CREATED, true);
            }
            c.sendBroadcast(i);
        }
    }

    @Override
    public void processFailure(Context c, RetrofitError e) {
        String msg = parseErrorMessage(e);
        Intent i = new Intent(FAIL_INTENT);
        if ("password incorrect".equals(msg)) {
            i.putExtra(EXTRA_INVALID_CREDENTIALS, true);
        }
        c.sendBroadcast(i);
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

    // return true if new user has been found and saved
    private boolean saveNewUserInfoIfNecessary(SharedPreferences prefs, User result) {
        if (result.password != null) {
            // password is only populated if an anonymous user was just created
            prefs.edit()
                    .putString(PrefKeys.NICKNAME, result.nickname)
                    .putString(PrefKeys.PASSWORD, result.password)
                    .putBoolean(PrefKeys.PASSWORD_IS_GENERATED, true)
                    .apply();
            return true;
        }
        // save the user id so it can be used by AccountFragment
        prefs.edit().putLong(PrefKeys.USER_ID, result.id).apply();
        // TODO remove this in a subsequent version, it's here to update the prefs of beta testers
        // with older versions
        prefs.edit().putBoolean(PrefKeys.PASSWORD_IS_GENERATED, true).apply();
        return false;
    }
}
