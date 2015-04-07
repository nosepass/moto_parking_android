package com.github.nosepass.motoparking.http;

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
    public Date createdAt;
    public Date updatedAt;
}
