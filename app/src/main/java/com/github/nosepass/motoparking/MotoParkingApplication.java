package com.github.nosepass.motoparking;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;


public class MotoParkingApplication extends Application {
    private static final String TAG = "MotoParkingApplication";
    private static final String CLSNAME = MotoParkingApplication.class.getName();

    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        MyLog.v(TAG, "onCreate");
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //prefs.edit().clear().commit(); // reset to defaults
        //PreferenceManager.setDefaultValues(this, R.xml.prefs, true);
        registerActivityLifecycleCallbacks(lifecycleCallbacks);

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