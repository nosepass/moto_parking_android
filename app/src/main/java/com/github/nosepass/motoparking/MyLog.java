package com.github.nosepass.motoparking;

import android.util.Log;

/**
 * Wrapper around {@link android.util.Log}, allowing
 * me to potentially redirect log output and add a couple convenience features.
 */
public class MyLog {

    private MyLog() {
    }

    public static int v(String tag, String msg) {
        return Log.v(tag, msg);
    }

    /**
     * Convenience method that uses {@link String#format(String, Object...)}
     */
    public static int v(String tag, String msg, Object ... formatArgs) {
        if (formatArgs == null || formatArgs.length == 0) {
            return Log.v(tag, msg);
        } else {
            String fmsg = String.format(msg, formatArgs);
            return Log.v(tag, fmsg);
        }
    }

    public static int d(String tag, String msg) {
        return Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return Log.d(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        return Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return Log.i(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        return Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return Log.w(tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        return Log.w(tag, tr);
    }

    public static int e(String tag, String msg) {
        return Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return Log.e(tag, msg, tr);
    }

    // this one isn't in the usual api
    public static int e(String tag, Throwable tr) {
        return Log.e(tag, "", tr);
    }

    /**
     * Checks to see whether or not a log for the specified tag is loggable at the specified level.
     *
     *  The default level of any tag is set to INFO. This means that any level above and including
     *  INFO will be logged. Before you make any calls to a logging method you should check to see
     *  if your tag should be logged. You can change the default level by setting a system property:
     *      'setprop log.tag.&lt;YOUR_LOG_TAG> &lt;LEVEL>'
     *  Where level is either VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT, or SUPPRESS. SUPPRESS will
     *  turn off all logging for your tag. You can also create a local.prop file that with the
     *  following in it:
     *      'log.tag.&lt;YOUR_LOG_TAG>=&lt;LEVEL>'
     *  and place that in /data/local.prop.
     *
     * @param tag The tag to check.
     * @param level The level to check.
     * @return Whether or not that this is allowed to be logged.
     * @throws IllegalArgumentException is thrown if the tag.length() > 23.
     */
    //public static native boolean isLoggable(String tag, int level);
}
