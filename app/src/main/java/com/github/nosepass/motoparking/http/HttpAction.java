package com.github.nosepass.motoparking.http;

import android.content.Context;
import android.content.Intent;

import com.github.nosepass.motoparking.MyUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import retrofit.RetrofitError;

/**
 * An http request that will be executed by a service.
 */
public abstract class HttpAction {
    //private static final String TAG = "HttpAction";
    protected static final String COMPLETE_SFX = ".COMPLETE";
    protected static final String FAIL_SFX = ".FAIL";
    public static final Gson gson = MyUtil.gson;

    public int attempts = 0;

    public abstract void executeHttpRequest(Apis apis) throws RetrofitError;

    /**
     * Override to process the response when the http request has completed successfully
     * @param c context to send broadcasts with
     */
    public void processResponse(Context c) {}

    /**
     * Override to control how failure is broadcast
     * @param c context to send broadcasts with
     */
    public void processFailure(Context c, RetrofitError e) {
        Intent i = new Intent(getClass().getName() + FAIL_SFX);
        c.sendBroadcast(i);
    }

    /**
     * Upon seeing a 403 error, the service will try to login first.
     * This flag disables the login behavior for the actual login action.
     * @return true if nothing should be done on 403, false if a login should be attempted
     */
    public boolean isLoginAction() {
        return false;
    }

    public String toString() {
        return getClass().getSimpleName() + toJson();
    }

    /**
     * Serialize into a json form that can be used by a service to reinflate the object.
     * A "class" key is added so the service knows what type of object to recreate.
     * Override to serialize parameters needed for your particular request.
     */
    public String toJson() {
        return toJson(new Object());
    }

    /**
     * Serializes params into json, then adds the "class" key used by services.
     */
    protected String toJson(Object params) {
        JsonElement json = gson.toJsonTree(params);
        json.getAsJsonObject().addProperty("class", getClass().getName());
        return gson.toJson(json);
    }

    /**
     * Try to parse an error message from a RetrofitError. Returns null if no message is found
     */
    protected String parseErrorMessage(RetrofitError e) {
        try {
            ErrorResponse er = (ErrorResponse) e.getBodyAs(ErrorResponse.class);
            return er.message;
        } catch (Exception ignored) {}
        return null;
    }
}