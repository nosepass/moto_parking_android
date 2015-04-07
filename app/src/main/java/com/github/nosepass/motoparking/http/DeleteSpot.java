package com.github.nosepass.motoparking.http;

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

    @Override
    public void executeHttpRequest(Apis apis) {
        MyLog.v(TAG, "downloading stuff");
        Object response = apis.parkingSpotApi.delete(spot.getId());
        MyLog.v(TAG, "got response " + response);
    }

    public String toJson() {
        return toJson(spot);
    }
}
