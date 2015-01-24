package com.github.nosepass.motoparking;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.github.nosepass.motoparking.db.LocalStorageService;
import com.github.nosepass.motoparking.http.HttpService;
import com.github.nosepass.motoparking.http.Login;
import com.github.nosepass.motoparking.http.LoginApi;
import com.github.nosepass.motoparking.http.ParkingDbDownload;
import com.github.nosepass.motoparking.http.ParkingSpotApi;
import com.github.nosepass.motoparking.http.SyncQueue;
import com.github.nosepass.motoparking.http.UserApi;
import com.github.nosepass.motoparking.util.ForegroundManager;
import com.google.android.gms.maps.model.LatLng;

import io.fabric.sdk.android.Fabric;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;


public class MotoParkingApplication extends Application {
    private static final String TAG = "MotoParkingApplication";
    //private static final String CLSNAME = MotoParkingApplication.class.getName();

    public static LoginApi loginApi;
    public static UserApi userApi;
    public static ParkingSpotApi parkingSpotApi;

    private SharedPreferences prefs;
    public ForegroundManager fgManager;

    @Override
    public void onCreate() {
        try {
            Crashlytics crashlytics = new Crashlytics.Builder().disabled(BuildConfig.DEBUG).build();
            Fabric.with(this, crashlytics);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
        MyLog.v(TAG, "onCreate");
        super.onCreate();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //prefs.edit().clear().commit(); // reset to defaults
        // overwrite old api url since that server died
        String url = prefs.getString(PrefKeys.BASE_URL, "");
        if (TextUtils.equals(url, "http://94.23.35.76:8080/")) {
            prefs.edit().remove(PrefKeys.BASE_URL).apply();
        }
        PreferenceManager.setDefaultValues(this, R.xml.prefs, true);

        fgManager = new ForegroundManager(this);

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(prefs.getString(PrefKeys.BASE_URL, ""))
                .setConverter(new GsonConverter(MyUtil.gson))
                .build();
        loginApi = restAdapter.create(LoginApi.class);
        userApi = restAdapter.create(UserApi.class);
        parkingSpotApi = restAdapter.create(ParkingSpotApi.class);

        try {
            // TODO pass this in in a better way
            HttpService.uploadQueue = new SyncQueue(this);
            updateParkingDb();
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    private void updateParkingDb() {
        LatLng startingLoc = MyUtil.getPrefLatLng(prefs, PrefKeys.STARTING_LAT_LONG);
        LocalStorageService.sendInitDb(this);
        //HttpService.addSyncAction(this, new ParkingDbDownload(startingLoc.latitude, startingLoc.longitude));
        HttpService.uploadQueue.add(new Login(prefs, (android.telephony.TelephonyManager) getSystemService(TELEPHONY_SERVICE)));
        HttpService.uploadQueue.add(new ParkingDbDownload(startingLoc.latitude, startingLoc.longitude));
    }
}