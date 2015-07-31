package com.github.nosepass.motoparking;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.bugsnag.android.Bugsnag;
import com.github.nosepass.motoparking.db.LocalStorageService;
import com.github.nosepass.motoparking.http.HttpService;
import com.github.nosepass.motoparking.http.Login;
import com.github.nosepass.motoparking.http.ParkingDbDownload;
import com.github.nosepass.motoparking.http.SyncQueue;
import com.github.nosepass.motoparking.util.ForegroundManager;
import com.google.android.gms.maps.model.LatLng;


public class MotoParkingApplication extends Application {
    private static final String TAG = "MotoParkingApplication";
    //private static final String CLSNAME = MotoParkingApplication.class.getName();

    private SharedPreferences prefs;
    public ForegroundManager fgManager;

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate");
        super.onCreate();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //prefs.edit().clear().commit(); // reset to defaults
        // overwrite old api url since that server died
        String url = prefs.getString(PrefKeys.BASE_URL, "");
        if (TextUtils.equals(url, "http://94.23.35.76:8080/")) {
            prefs.edit().remove(PrefKeys.BASE_URL).apply();
        }
        PreferenceManager.setDefaultValues(this, R.xml.prefs, true);

        try {
            Bugsnag.init(this);
            long userid = prefs.getLong(PrefKeys.USER_ID, -1);
            String nick = prefs.getString(PrefKeys.NICKNAME, "");
            if (!TextUtils.isEmpty(nick)) {
                Bugsnag.setUser(userid + "", null, nick);
            }
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            Bugsnag.addToTab("Device", "Device ID", tm.getDeviceId());
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }

        fgManager = new ForegroundManager(this);

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