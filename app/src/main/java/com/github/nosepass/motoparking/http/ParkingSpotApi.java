package com.github.nosepass.motoparking.http;

import com.github.nosepass.motoparking.db.ParcelableParkingSpot;
import com.github.nosepass.motoparking.db.ParkingSpot;

import java.util.ArrayList;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * Retrieve a list of relevant parking spots from the server to store locally.
 */
public interface ParkingSpotApi {
    @GET("/parking_spots.json")
    ArrayList<ParcelableParkingSpot> getSpots();
    @POST("/parking_spots.json")
    ParkingSpot create(@Body ParkingSpotParameters params);
    @PUT("/parking_spots/{id}.json")
    ParkingSpot update(@Path("id") String id, @Body ParkingSpotParameters params);
    @DELETE("/parking_spots/{id}.json")
    Object delete(@Path("id") String id);

    /**
     * Wrapper to format json with a parking_spot root key that Rails likes.
     */
    class ParkingSpotParameters {
        ParkingSpot parkingSpot;

        public ParkingSpotParameters(ParkingSpot parkingSpot) {
            this.parkingSpot = parkingSpot;
        }
    }
}
