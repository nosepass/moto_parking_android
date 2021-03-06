package com.github.nosepass.motoparking.http;

import com.github.nosepass.motoparking.MyLog;
import com.github.nosepass.motoparking.db.ParkingSpot;
import com.google.gson.JsonObject;

/**
 * Update a parking spot on the server.
 */
public class EditSpot extends HttpAction {
    private static final String TAG = "http.EditSpot";
    ParkingSpot spot;
    ParkingSpot response;

    public EditSpot(ParkingSpot newSpot) {
        this.spot = newSpot;
    }

    public EditSpot(JsonObject json) {
        this.spot = gson.fromJson(json, ParkingSpot.class);
    }

    @Override
    public void executeHttpRequest(Apis apis) {
        MyLog.v(TAG, "downloading stuff");
        response = apis.parkingSpotApi.update(spot.getId(), new ParkingSpotApi.ParkingSpotParameters(spot));
        MyLog.v(TAG, "" + response);
    }

    public String toJson() {
        return toJson(spot);
    }
}
