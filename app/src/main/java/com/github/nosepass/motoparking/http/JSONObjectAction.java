package com.github.nosepass.motoparking.http;

import android.content.SharedPreferences;

import com.github.nosepass.motoparking.MyLog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An http request that results in a JSONObject
 */
public abstract class JSONObjectAction extends BaseUrlAction<JSONObject> {
    private static final String TAG = "http.JSONObjectAction";

    public JSONObjectAction(SharedPreferences prefs) {
        super(prefs);
    }

    @Override
    public void parseResult() {
        try {
            if (resultString != null) {
                result = new JSONObject(resultString);
            }
        } catch (JSONException e) {
            MyLog.e(TAG, e);
            errors = retryJsonParseErrors();
        }
    }

    protected boolean retryJsonParseErrors() {
        return true;
    }
}
