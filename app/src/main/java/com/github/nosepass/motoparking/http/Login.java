package com.github.nosepass.motoparking.http;

import android.content.SharedPreferences;

import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONObject;

/**
 * Hit the login url to establish a session on the server
 */
public class Login extends JSONObjectAction {
    private static final String TAG = "http.Login";

    public Login(SharedPreferences prefs, String nickname, String password, String deviceId) {
        super(prefs);
        JSONObject creds = buildJson(
                "nickname", nickname,
                "password", password);
        JSONObject phoneInfo = new PhoneInfoBuilder().buildInfo(deviceId);
        buildParams("creds", creds.toString(), "phone_info", phoneInfo.toString());
    }

    @Override
    public void onSuccess() {
        if (result != null) {
            saveNewUserInfoIfNecessary(result);
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

    private void saveNewUserInfoIfNecessary(JSONObject result) {
        // TODO
    }
}
