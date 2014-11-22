package com.github.nosepass.motoparking.http;

import android.content.SharedPreferences;

import com.github.nosepass.motoparking.db.ParkingSpot;

import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;

import java.util.List;

import retrofit.http.GET;

/**
 * Retrieve a list of relevant parking spots from the server to store locally.
 */
public interface ParkingSpotApi {
    @GET("/parking_spots.json")
    List<ParkingSpot> getSpots();
}
