package com.github.nosepass.motoparking.http;

import android.content.SharedPreferences;

import com.github.nosepass.motoparking.PrefKeys;


public abstract class BaseUrlAction<T> extends HttpAction<T> {
    private static final String TAG = "http.BaseUrlAction";

    protected String baseUrl;

    public BaseUrlAction(SharedPreferences prefs) {
        baseUrl = prefs.getString(PrefKeys.BASE_URL, "http://");
    }

    public BaseUrlAction(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    protected String getCookieUrl() {
        return baseUrl;
    }
}