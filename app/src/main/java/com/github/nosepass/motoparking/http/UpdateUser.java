package com.github.nosepass.motoparking.http;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.github.nosepass.motoparking.MyLog;
import com.github.nosepass.motoparking.PrefKeys;
import com.google.gson.JsonObject;

import retrofit.RetrofitError;

/**
 * Change a user
 * Hit the login url to establish a session on the server
 */
public class UpdateUser extends HttpAction {
    private static final String TAG = "http.UpdateUser";
    private static final String CLSNAME = UpdateUser.class.getName();
    public static final String COMPLETE_INTENT = CLSNAME + ".COMPLETE";
    public static final String FAIL_INTENT = CLSNAME + FAIL_SFX;

    public static class UserParameters {
        User user = new User();
        PhoneInfo phoneInfo;

        public UserParameters() { }

        public UserParameters(long id, String nickname, String deviceId) {
            user.nickname = nickname;
            phoneInfo = new PhoneInfo(deviceId);
        }
    }

    UserParameters params;

    /**
     * Change the user's nickname. The old nickname is required to look up the user record.
     */
    public UpdateUser(long id, String newNickname, String deviceId) {
        this.params = new UserParameters(id, newNickname, deviceId);
    }

    public UpdateUser(JsonObject json) {
        this.params = gson.fromJson(json, UserParameters.class);
    }

    @Override
    public void executeHttpRequest(Apis apis) {
        MyLog.v(TAG, "downloading stuff");
        Object response = apis.userApi.update(params.user.id, params);
        MyLog.v(TAG, "" + response);
    }

    @Override
    public void processResponse(Context c) {
        // save the new nickname
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putString(PrefKeys.NICKNAME, params.user.nickname).apply();
        Intent i = new Intent(COMPLETE_INTENT);
        c.sendBroadcast(i);
    }

    @Override
    public void processFailure(Context c, RetrofitError e) {
        Intent i = new Intent(FAIL_INTENT);
        c.sendBroadcast(i);
    }

    // don't log the password with the superclass toString()
    public String toString() {
        return getClass().getSimpleName();
    }

    public String toJson() {
        return toJson(params);
    }
}
