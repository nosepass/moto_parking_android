package com.github.nosepass.motoparking;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.List;

public class MyUtil {
    private static final String TAG = "MyUtil";

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
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

            } else {
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

    public static void showToast(Fragment _this, final String msg) {
        final Activity a = _this.getActivity();
        if (a != null) {
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(a, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Return the distance between two points in kmeters, using the Haversine formula.
     */
    public static double _distFrom(double lat1, double lng1, double lat2, double lng2) {
        //double earthRadius = 3958.75; // this returns miles
        double earthRadius = 6372.8; // km
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
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
}
