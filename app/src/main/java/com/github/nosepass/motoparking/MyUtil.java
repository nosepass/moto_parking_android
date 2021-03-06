package com.github.nosepass.motoparking;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Build;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import com.github.nosepass.motoparking.views.MaterialProgressDrawable;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.internal.bind.DateTypeAdapter;

import org.json.JSONObject;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Date;
import java.util.List;

public class MyUtil {
    private static final String TAG = "MyUtil";
    public static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            //.setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .registerTypeAdapter(Date.class, new DateTypeAdapter())
            //.registerTypeAdapter(Double.class, new DoubleTypeAdapter())
            .create();
    public static final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    public static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference != null) {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    public static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * Thread.sleep() with the assumption that Thread.interrupt() won't
     * be called;
     */
    public static void sleepUninterrupted(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            MyLog.e(TAG, e);
        }
    }

    public static boolean isAppInstalled(Context c, String packageUri) {
        PackageManager pm = c.getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(packageUri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    public static boolean isAppRunning(Context c, String packageUri) {
        ActivityManager am = (ActivityManager) c.getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procs = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo process : procs) {
            if (process.processName != null && process.processName.equals(packageUri)) {
                return true;
            }
        }
        return false;
    }

    public static float parseFloatProperty(JSONObject json, String prop) {
        try {
            return Float.parseFloat(json.optString(prop));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Return the distance between two points in kmeters, using the Haversine formula.
     */
    public static double _distFrom(double lat1, double lng1, double lat2, double lng2) {
        //double earthRadius = 3958.75; // this returns miles
        double earthRadius = 6372.8; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    /**
     * Return the distance between two points in meters.
     */
    public static float geographicDistance(double lat1, double lng1, double lat2, double lng2) {
        Location l1 = new Location(""), l2 = new Location("");
        l1.setLatitude(lat1);
        l1.setLongitude(lng1);
        l2.setLatitude(lat2);
        l2.setLongitude(lng2);
        return l1.distanceTo(l2);
    }

    public static LatLng getPrefLatLng(SharedPreferences prefs, String prefKey) {
        LatLng latLong = new LatLng(37.757687, -122.436104); // default to SF
        String prefLL = prefs.getString(prefKey, "");

        if (prefLL != null && prefLL.split(",").length > 1) {
            String[] split = prefLL.split(",");
            try {
                double lat = Double.parseDouble(split[0]);
                double lng = Double.parseDouble(split[1]);
                latLong = new LatLng(lat, lng);
            } catch (NumberFormatException e) {
                MyLog.e(TAG, e);
            }
        }

        return latLong;
    }

    public static boolean beforeLollipop() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isNetworkAvailable(ConnectivityManager cm) {
        if (cm == null) {
            // somehow ConnectivityManager itself can be null, prolly on stupid Samsung devices
            // assume connected in that case
            MyLog.e(TAG, "null ConnectivityManager");
            return true;
        } else {
            return cm.getActiveNetworkInfo() != null
                    && cm.getActiveNetworkInfo().isConnected();
        }
    }

    public static JsonElement getBuildInfo() {
        return new GsonBuilder()
                // include static fields
                .excludeFieldsWithModifiers()
                .create().toJsonTree(new Build());
    }

    @SuppressWarnings("unchecked")
    public static Drawable getMaterialStyleLoaderIfNotLollipop(Context c, View parent) {
        // Use a copy of the hidden support-v4 Material loader drawable
        // TODO use support-v4 via reflection instead of copying?
        if (MyUtil.beforeLollipop()) {
            MaterialProgressDrawable mpd = new MaterialProgressDrawable(c, parent);
            mpd.setBackgroundColor(0xFFFAFAFA);
            mpd.setAlpha(128);
            mpd.start();
            return mpd;
        }
        return null;
    }
}
