package com.github.nosepass.motoparking.http;

import android.content.SharedPreferences;

import com.github.nosepass.motoparking.MotoParkingApplication;
import com.github.nosepass.motoparking.MyLog;
import com.github.nosepass.motoparking.PrefKeys;

import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONObject;

/**
 * Hit the login url to establish a session on the server
 */
public class Login extends JSONObjectAction {
    private static final String TAG = "http.Login";
    SharedPreferences prefs;
    LoginParameters params2;
    UserResponse result2;

    public Login(SharedPreferences prefs, String nickname, String password, String deviceId) {
        super(prefs);
        this.prefs = prefs;
        JSONObject creds = buildJson(
                "nickname", nickname,
                "password", password);
        JSONObject phoneInfo = new PhoneInfoBuilder().buildInfo(deviceId);
        buildParams("creds", creds.toString(), "phone_info", phoneInfo.toString());
        params2 = new LoginParameters(nickname, password, deviceId);
    }

    public void executeHttpRequest() {
        // hacktastic
        try {
            MyLog.v(TAG, "downloading stuff");
            LoginApi api = MotoParkingApplication.loginApi;
            if (api != null) {
                UserResponse user = api.login(params2);
                MyLog.v(TAG, "" + user);
                result2 = user;
                statusCode = 200;
            } else {
                throw new Exception("no retrofit api found!");
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
            errors = true;
            //exception = e;
        }
    }

    @Override
    public void onSuccess() {
        if (result2 != null) {
            saveNewUserInfoIfNecessary(result2);
        }
    }

    @Override
    protected HttpUriRequest createRequest() {
        String url = baseUrl +  "/login.json";
        return createPostWithFormParams(url);
    }

    protected boolean retryJsonParseErrors() {
        return true;
    }

    private void saveNewUserInfoIfNecessary(UserResponse result) {
        prefs.edit().putString(PrefKeys.NICKNAME, result.nickname).apply();
        // TODO save generated password
    }
}
