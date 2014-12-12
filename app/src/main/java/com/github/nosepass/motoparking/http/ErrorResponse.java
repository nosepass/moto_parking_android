package com.github.nosepass.motoparking.http;

/**
 * Some api endpoints return json error messages. Retrofit can parse them into here using Gson.
 */
public class ErrorResponse {
    public String message;
}
