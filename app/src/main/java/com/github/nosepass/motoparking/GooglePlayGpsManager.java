package com.github.nosepass.motoparking;


import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

/**
 * Gets a mostly-precise location reading then idles the gps
 */
public class GooglePlayGpsManager implements LocationListener {
    private static final String TAG = "GooglePlayGpsManager";
    //private static final String CLSNAME = GooglePlayGpsManager.class.getName();
    private static final long GPS_TOO_LONG = 2 * 60 * 1000;

    private Context context;
    private LocationClient locationClient;
    private long startTime;
    private float desiredAccuracy;
    private AccurateLocationFoundCallback callback;


    public GooglePlayGpsManager(Context context) {
        this.context = context;
        try {
            initLocationClient();
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    /**
     * Turn on the gps and try to get a fix that is at least as accurate
     * as desiredAccuracy, calling callback when that is found.
     * desiredAccuracy and callback are implicitly set on this instance.
     */
    public void start(float desiredAccuracy, AccurateLocationFoundCallback callback) {
        MyLog.v(TAG, "start");
        startTime = System.currentTimeMillis();
        this.desiredAccuracy = desiredAccuracy;
        this.callback = callback;
        try {
            initLocationClient();
            requestFastLocationUpdates();
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    public void stop() {
        stopLocationUpdates();
    }

    private void requestFastLocationUpdates() {
        MyLog.v(TAG, "requestFastLocationUpdates");
        if (locationClient != null && locationClient.isConnected()) {
            locationClient.removeLocationUpdates(this);
            LocationRequest lr = LocationRequest.create();
            long interval = 5 * 1000;
            lr.setInterval(interval);
            lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationClient.requestLocationUpdates(lr, this);
        } else {
            MyLog.w(TAG, "could not requestLocationUpdates");
        }
    }

    private void stopLocationUpdates() {
        MyLog.v(TAG, "stopLocationUpdates");
        if (locationClient != null && locationClient.isConnected()) {
            locationClient.removeLocationUpdates(this);
        } else {
            MyLog.w(TAG, "could not stopLocationUpdates");
        }
    }

    @Override
    public void onLocationChanged(Location l) {
        MyLog.v(TAG, "onLocationChanged " + l);
        String loc = l.getLatitude() + "," + l.getLongitude();
        boolean isAccurate = l.getAccuracy() <= desiredAccuracy;
        if (isAccurate) {
            MyLog.v(TAG, "got accurate reading " + l.getAccuracy() + " " + loc);
            if (callback != null) {
                try {
                    callback.onAccurateLocationFound(l);
                } catch (Exception e) {
                    MyLog.e(TAG, e);
                }
            }
            stopLocationUpdates();
        } else {
            MyLog.v(TAG, "ignoring reading not accurate enough " + l.getAccuracy() + " " + loc);
        }
        long now = System.currentTimeMillis();
        long delta = now - startTime;
        if (delta > GPS_TOO_LONG) {
            // no fix in 2 mins? lulz
            MyLog.w(TAG, "could not get fix within two mins, giving up");
            stopLocationUpdates();
        }
    }

    private void initLocationClient() {
        if (locationClient == null) {
            int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
            if (result == ConnectionResult.SUCCESS) {
                locationClient = new LocationClient(context, connectionCallbacks, new GooglePlayServicesClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        MyLog.e(TAG, "onConnectonFailed");
                        reportGooglePlayError(connectionResult.getErrorCode());
                    }
                });
                locationClient.connect();
            } else {
                reportGooglePlayError(result);
            }
        }
        if (locationClient != null) {
            if ( !(locationClient.isConnected() || locationClient.isConnecting()) ) {
                locationClient.connect();
            }
        }
    }

    private void reportGooglePlayError(int error) {
        MyLog.e(TAG, "google play services unavailable: " + error);
        Intent i = new Intent(context, GmsErrorActivity.class);
        i.putExtra("error", error);
        context.startActivity(i);
    }

    private GooglePlayServicesClient.ConnectionCallbacks connectionCallbacks = new GooglePlayServicesClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            MyLog.v(TAG, "onConnected");
            requestFastLocationUpdates();
        }

        @Override
        public void onDisconnected() {
            MyLog.v(TAG, "onDisconnected");
        }
    };

    public static interface AccurateLocationFoundCallback {
        public void onAccurateLocationFound(Location l);
    }
}
