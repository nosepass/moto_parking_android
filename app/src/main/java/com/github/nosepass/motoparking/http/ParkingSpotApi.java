package com.github.nosepass.motoparking.http;

import android.content.SharedPreferences;

import com.github.nosepass.motoparking.db.ParkingSpot;

import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;

/**
 * Retrieve a list of relevant parking spots from the server to store locally.
 */
public interface ParkingSpotApi {
    @GET("/parking_spots.json")
    List<ParkingSpot> getSpots();
    @POST("/parking_spots.json")
    ParkingSpot create(@Body ParkingSpot spot);
}
