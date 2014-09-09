package com.github.nosepass.motoparking.http;

import android.content.SharedPreferences;

import com.github.nosepass.motoparking.MyLog;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * An http request that results in a JSONArray response
 */
public abstract class JSONArrayAction extends BaseUrlAction<JSONArray> {
    private static final String TAG = "http.JSONArrayAction";

    public JSONArrayAction(SharedPreferences prefs) {
        super(prefs);
    }

    @Override
    public void parseResult() {
        try {
            result =  new JSONArray(resultString);
        } catch (JSONException e) {
            MyLog.e(TAG, e);
            errors = retryJsonParseErrors();
        }
    }

    protected boolean retryJsonParseErrors() {
        return true;
    }
}
