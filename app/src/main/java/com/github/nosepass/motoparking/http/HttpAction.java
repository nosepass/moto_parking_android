package com.github.nosepass.motoparking.http;

import android.content.Context;

import com.github.nosepass.motoparking.MyUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import retrofit.RetrofitError;

/**
 * An http request that will be executed by a service.
 */
public abstract class HttpAction {
    //private static final String TAG = "HttpAction";
    public static final Gson gson = MyUtil.gson;

    public int attempts = 0;

    public abstract void executeHttpRequest() throws RetrofitError;

    /**
     * Override to process the response when the http request has completed successfully
     * @param c context to send broadcasts with
     */
    public void processResponse(Context c) {}

    public boolean isLoginAction() {
        return false;
    }

    public String toString() {
        return getClass().getSimpleName() + toJson();
    }

    /**
     * Serialize into a json form that can be used by a service to reinflate the object.
     * A "class" key is added so the service knows what type of object to recreate.
     */
    public String toJson() {
        JsonObject json = new JsonObject();
        json.getAsJsonObject().addProperty("class", getClass().getName());
        return gson.toJson(json);
    }

    /**
     * Serializes params into json, then adds the "class" key used by services.
     */
    protected String toJson(Object params) {
        JsonElement json = gson.toJsonTree(params);
        json.getAsJsonObject().addProperty("class", getClass().getName());
        return gson.toJson(json);
    }

}