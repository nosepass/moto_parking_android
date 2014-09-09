package com.github.nosepass.motoparking.http;

import android.content.SharedPreferences;

import com.github.nosepass.motoparking.MyLog;

import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONException;
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
    public void parseResult() {
        try {
            if (resultString != null) {
                result = new JSONObject(resultString);
                saveNewUserInfoIfNecessary(result);
            }
        } catch (JSONException e) {
            MyLog.e(TAG, e);
            errors = retryJsonParseErrors();
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
