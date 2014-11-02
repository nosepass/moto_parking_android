package com.github.nosepass.motoparking.http;

import android.content.SharedPreferences;

import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;

/**
 * Retrieve a list of relevant parking spots from the server to store locally.
 */
public class ParkingDbDownload extends JSONArrayAction {
    private static final String TAG = "http.ParkingDbDownload";

    public ParkingDbDownload(SharedPreferences prefs, String lat, String longitude) {
        super(prefs);
        buildParams("latitude", lat, "longitude", longitude);
    }

    @Override
    public void onSuccess() {
        if (result != null) {
            saveSpotsToDb(result);
        }
    }

    @Override
    protected HttpUriRequest createRequest() {
        String url = baseUrl +  "/parking_spots.json";
        return createHttpGetWithQueryParams(url);
    }

    protected boolean retryJsonParseErrors() {
        return true;
    }

    private void saveSpotsToDb(JSONArray result) {
        // TODO
    }
}
