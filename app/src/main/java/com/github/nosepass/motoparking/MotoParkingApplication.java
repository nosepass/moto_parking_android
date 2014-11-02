package com.github.nosepass.motoparking;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.github.nosepass.motoparking.http.HttpAction;
import com.github.nosepass.motoparking.http.Login;
import com.github.nosepass.motoparking.http.ParkingDbDownload;
import com.github.nosepass.motoparking.http.SyncQueue;
import com.github.nosepass.motoparking.http.SyncThread;
import com.google.android.gms.maps.model.LatLng;


public class MotoParkingApplication extends Application {
    private static final String TAG = "MotoParkingApplication";
    //private static final String CLSNAME = MotoParkingApplication.class.getName();

    private static MotoParkingApplication instance;

    private SharedPreferences prefs;
    private SyncQueue syncQueue;

    @Override
    public void onCreate() {
        MyLog.v(TAG, "onCreate");
        super.onCreate();
        instance = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //prefs.edit().clear().commit(); // reset to defaults
        //PreferenceManager.setDefaultValues(this, R.xml.prefs, true);
        registerActivityLifecycleCallbacks(lifecycleCallbacks);
        try {
            syncQueue = new SyncQueue(this);
            SyncThread syncThread = new SyncThread(this, syncQueue);
            syncThread.start();
            login();
            updateParkingDb();
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    /**
     * Add an http request so it can be executed on the sync thread.
     */
    public static void addSyncAction(HttpAction action) {
        instance.syncQueue.add(action);
    }

    private void login() {
        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        String nickname = prefs.getString(PrefKeys.NICKNAME, "");
        String pw = prefs.getString(PrefKeys.PASSWORD, "");
        addSyncAction(new Login(prefs, nickname, pw, deviceId));
    }

    private void updateParkingDb() {
        LatLng startingLoc = MyUtil.getInitialLatLng(prefs);
        String lat = startingLoc.latitude + "";
        String lng = startingLoc.longitude + "";
        addSyncAction(new ParkingDbDownload(prefs, lat, lng));
    }

    private Application.ActivityLifecycleCallbacks lifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            MyLog.v(TAG, activity.getLocalClassName() + " onCreate");
        }

        @Override
        public void onActivityStarted(Activity activity) {
            MyLog.v(TAG, activity.getLocalClassName() + " onStart");
        }

        @Override
        public void onActivityResumed(Activity activity) {
            MyLog.v(TAG, activity.getLocalClassName() + " onResume");
        }

        @Override
        public void onActivityPaused(Activity activity) {
            MyLog.v(TAG, activity.getLocalClassName() + " onPause");
        }

        @Override
        public void onActivityStopped(Activity activity) {
            MyLog.v(TAG, activity.getLocalClassName() + " onStop");
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
        @Override
        public void onActivityDestroyed(Activity activity) {
            MyLog.v(TAG, activity.getLocalClassName() + " onDestroy");
        }
    };
}