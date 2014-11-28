package com.github.nosepass.motoparking.util;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.github.nosepass.motoparking.MyLog;

/**
 * Just logs onServiceConnected and onServiceDisconnected
 */
public class LoggingServiceConnection implements ServiceConnection {
    private static final String TAG = "androidlib.util.LoggingServiceConnection";

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MyLog.v(TAG, "onServiceConnected %s %s", name.getShortClassName(), service + "");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        MyLog.v(TAG, "onServiceDisconnected %s %s", name.getShortClassName());
    }
}

