package com.github.nosepass.motoparking.http;

import android.content.Context;

import com.github.nosepass.motoparking.MotoParkingApplication;
import com.github.nosepass.motoparking.MyLog;
import com.github.nosepass.motoparking.db.ParkingSpot;
import com.google.gson.JsonObject;

/**
 * Add a parking spot
 * Hit the login url to establish a session on the server
 */
public class AddSpot extends HttpAction {
    private static final String TAG = "http.AddSpot";
    ParkingSpot params;
    ParkingSpot response;

    public AddSpot(ParkingSpot newSpot) {
        this.params = newSpot;
    }

    public AddSpot(JsonObject json) {
        this.params = gson.fromJson(json, ParkingSpot.class);
    }

    public void executeHttpRequest() {
        MyLog.v(TAG, "downloading stuff");
        ParkingSpotApi api = MotoParkingApplication.parkingSpotApi;
        response = api.create(new ParkingSpotApi.ParkingSpotParameters(params));
        MyLog.v(TAG, "" + response);
    }

    @Override
    public void processResponse(Context c) {
        saveId(response);
    }

    public String toJson() {
        return toJson(params);
    }

    private void saveId(ParkingSpot result) {
        // TODO save
    }
}
