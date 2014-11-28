package com.github.nosepass.motoparking.util;


import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.github.nosepass.motoparking.MyLog;

/**
 * Keep track of whether the app has an activity in the foreground
 * by counting onStarts etc.
 */
public class ForegroundManager {
    private static final String TAG = "ForegroundManager";
    private static final String CLSNAME = ForegroundManager.class.getName();

    public static final String APP_IN_BACKGROUND = CLSNAME + ".APP_IN_BACKGROUND";
    public static final String APP_IN_FOREGROUND = CLSNAME + ".APP_IN_FOREGROUND";
    public static final String ACTIVITIES_DESTROYED = CLSNAME + ".ACTIVITIES_DESTROYED";

    private Application application;
    private Handler handler;

    private int activityCount = 0;
    private int fgActivityCount = 0;
    private boolean inBackground = true;
    private Activity active;

    public ForegroundManager(Application application) {
        this.application = application;
        MyLog.v(TAG, "onCreate");
        this.handler = new Handler();
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks);
    }

    private Application.ActivityLifecycleCallbacks lifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            MyLog.v(TAG, activity.getLocalClassName() + " onCreate");
            activityCount++;
        }

        @Override
        public void onActivityStarted(Activity activity) {
            MyLog.v(TAG, activity.getLocalClassName() + " onStart");
            fgActivityCount++;
            if (inBackground) {
                inBackground = false;
                application.sendBroadcast(new Intent(APP_IN_FOREGROUND));
            }
            active = activity;
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
            fgActivityCount--;
            handler.postDelayed(new Runnable(){
                @Override
                public void run() {
                    if (fgActivityCount == 0) {
                        inBackground = true;
                        application.sendBroadcast(new Intent(APP_IN_BACKGROUND));
                    }
                }
            }, 1000);
            if (fgActivityCount == 0) {
                active = null;
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
        @Override
        public void onActivityDestroyed(Activity activity) {
            MyLog.v(TAG, activity.getLocalClassName() + " onDestroy");
            activityCount--;
            if (activityCount == 0) {
                application.sendBroadcast(new Intent(ACTIVITIES_DESTROYED));
            }
        }
    };

    public Activity getActiveActivity() {
        return active;
    }
}
