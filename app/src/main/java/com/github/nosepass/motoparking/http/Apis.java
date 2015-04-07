package com.github.nosepass.motoparking.http;

import android.content.SharedPreferences;

import com.github.nosepass.motoparking.MyUtil;
import com.github.nosepass.motoparking.PrefKeys;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Include all Retrofit REST apis here
 */
public class Apis {
    final LoginApi loginApi;
    final UserApi userApi;
    final ParkingSpotApi parkingSpotApi;

    Apis(SharedPreferences prefs) {
        CookieHandler.setDefault(MyUtil.cookieManager);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(prefs.getString(PrefKeys.BASE_URL, "") + "")
                .setConverter(new GsonConverter(MyUtil.gson))
                .build();
        loginApi = restAdapter.create(LoginApi.class);
        userApi = restAdapter.create(UserApi.class);
        parkingSpotApi = restAdapter.create(ParkingSpotApi.class);
    }
}
