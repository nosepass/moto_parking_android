package com.github.nosepass.motoparking.http;

import com.github.nosepass.motoparking.MyLog;
import com.github.nosepass.motoparking.db.ParkingSpot;
import com.google.gson.JsonObject;

/**
 * Add a parking spot.
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

    @Override
    public void executeHttpRequest(Apis apis) {
        MyLog.v(TAG, "downloading stuff");
        response = apis.parkingSpotApi.create(new ParkingSpotApi.ParkingSpotParameters(params));
        MyLog.v(TAG, "" + response);
    }

    public String toJson() {
        return toJson(params);
    }
}
