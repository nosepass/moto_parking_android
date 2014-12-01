package com.github.nosepass.motoparking.http;

import com.github.nosepass.motoparking.MotoParkingApplication;
import com.github.nosepass.motoparking.MyLog;
import com.github.nosepass.motoparking.db.ParkingSpot;
import com.google.gson.JsonObject;

/**
 * Delete a parking spot on the server.
 */
public class DeleteSpot extends HttpAction {
    private static final String TAG = "http.DeleteSpot";
    ParkingSpot spot;

    public DeleteSpot(ParkingSpot newSpot) {
        this.spot = newSpot;
    }

    public DeleteSpot(JsonObject json) {
        this.spot = gson.fromJson(json, ParkingSpot.class);
    }

    public void executeHttpRequest() {
        MyLog.v(TAG, "downloading stuff");
        ParkingSpotApi api = MotoParkingApplication.parkingSpotApi;
        Object response = api.delete(spot.getId());
        MyLog.v(TAG, "got response " + response);
    }

    public String toJson() {
        return toJson(spot);
    }
}
