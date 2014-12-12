package com.github.nosepass.motoparking.http;

import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Hit the login url to start a session.
 */
public interface LoginApi {
    @POST("/login.json")
    User login(@Body LoginParameters params);
}
