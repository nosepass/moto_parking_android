package com.github.nosepass.motoparking;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

/**
 * Wrapper around {@link android.util.Log}, allowing
 * me to potentially redirect log output and add a couple convenience features.
 */
public class MyLog {
    private static final String TAG = "MyLog";

    private MyLog() {
    }

    public static void v(String tag, String msg) {
        log(Log.VERBOSE, tag, msg);
    }

    /**
     * Convenience method that uses {@link String#format(String, Object...)}
     */
    public static void v(String tag, String msg, Object... formatArgs) {
        if (formatArgs == null || formatArgs.length == 0) {
            v(tag, msg);
        } else {
            try {
                String fmsg = String.format(msg, formatArgs);
                v(tag, fmsg);
            } catch (Exception e) {
                v(tag, msg);
                e(TAG, e);
            }
        }
    }

    public static void d(String tag, String msg) {
        log(Log.DEBUG, tag, msg);
    }

    public static void i(String tag, String msg) {
        log(Log.INFO, tag, msg);
    }

    public static void  w(String tag, String msg) {
        log(Log.WARN, tag, msg);
    }

    public static void e(String tag, String msg) {
        log(Log.ERROR, tag, msg);
        logErrorOnline(new Exception(msg));
    }

    public static void e(String tag, String msg, Throwable tr) {
        log(Log.ERROR, tag, msg);
        logErrorOnline(tr);
    }

    // this one isn't in the usual api
    public static void e(String tag, Throwable tr) {
        log(Log.ERROR, tag, tr.getMessage());
        logErrorOnline(tr);
    }

    private static void log(int priority, String tag, String msg) {
        try {
            Crashlytics.log(priority, tag, msg);
        } catch (Exception e) {
            Log.println(priority, tag, msg);
            Log.e(TAG, "",  e);
        }
    }

    private static void logErrorOnline(Throwable t) {
        try {
            Crashlytics.logException(t);
        } catch (Exception e) {
            Log.e(TAG, "",  e);
        }
    }
}
