package com.github.nosepass.motoparking.http;

import android.content.Context;

import com.github.nosepass.motoparking.MotoParkingApplication;
import com.github.nosepass.motoparking.MyLog;
import com.github.nosepass.motoparking.db.DaoSession;
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
        // Save the server-generated id so edits can work properly
        // result.localId is null because the server doesn't care about the sqlite ids
        // so copy the server id over to an object that has localId populated.
        DaoSession s = ParkingDbDownload.daoMaster.newSession();
        params.setId(result.getId());
        s.getParkingSpotDao().update(params);
        // TODO broadcast when save complete
    }
}
