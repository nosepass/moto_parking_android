package com.github.nosepass.motoparking.http;

import android.content.Context;
import android.content.Intent;

import com.github.nosepass.motoparking.MyLog;
import com.github.nosepass.motoparking.db.LocalStorageService;
import com.github.nosepass.motoparking.db.ParcelableParkingSpot;
import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * Retrieve a list of relevant parking spots from the server to store locally.
 */
public class ParkingDbDownload extends HttpAction {
    private static final String TAG = "http.ParkingDbDownload";
    private static final String CLSNAME = ParkingDbDownload.class.getName();
    public static final String DOWNLOAD_COMPLETE = CLSNAME + ".DOWNLOAD_COMPLETE";

    private ArrayList<ParcelableParkingSpot> response;


    /**
     * @param lat current user's latitude
     * @param longitude current user's longitude
     */
    public ParkingDbDownload(double lat, double longitude) {
    }

    public ParkingDbDownload(JsonObject serialized) {
    }

    @Override
    public void executeHttpRequest(Apis apis) {
        MyLog.v(TAG, "downloading stuff");
        response = apis.parkingSpotApi.getSpots();
    }


    @Override
    public void processResponse(Context c) {
        LocalStorageService.sendSaveSpots(c, response);
        c.sendBroadcast(new Intent(DOWNLOAD_COMPLETE));
    }
}
