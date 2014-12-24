package com.github.nosepass.motoparking.http;

import org.json.JSONObject;

import java.util.Date;

/**
 * The data for a user's record
 */
public class User {
    //private static final String TAG = "http.User";

    public long id;
    public String nickname;
    public String password;
    public String fname;
    public String lname;
    public String email;
    // TODO fix the date transformation in Gson
//    public Date createdAt;
//    public Date updatedAt;
}