package com.github.nosepass.motoparking.http;

import com.github.nosepass.motoparking.db.ParkingSpot;

import retrofit.http.Body;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * Change a user on the server.
 */
public interface UserApi {
    @PUT("/users/{id}.json")
    ParkingSpot update(@Path("id") long id, @Body UpdateUser.UserParameters params);
}
