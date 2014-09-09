package com.github.nosepass.motoparking.http;

import android.content.Context;
import android.net.ConnectivityManager;

import com.github.nosepass.motoparking.MyLog;
import com.github.nosepass.motoparking.MyUtil;

/**
 * Carries out queued http requests immediately in a serial fashion.
 */
public class SyncThread extends Thread {
    private static final String TAG = "http.SyncThread";

    //private Context context;
    private ConnectivityManager cm;
    private SyncQueue syncQueue;
    //private boolean running;

    public SyncThread(Context appContext, SyncQueue syncQueue) {
        Context context = appContext;
        cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.syncQueue = syncQueue;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (true) {
            boolean connected = cm.getActiveNetworkInfo() != null
                    && cm.getActiveNetworkInfo().isConnected();
            if (!connected && syncQueue.hasActions()) {
                MyLog.v(TAG, "no network - trying again in 30secs");
                sleep(30);
                continue;
            }
            if (syncQueue.hasActions()) {
                MyLog.v(TAG, "syncing actions");
                SyncQueue.Status status = SyncQueue.Status.INTERNAL_ERRORS;
                try {
                    status = syncQueue.doSync();
                } catch (Exception e) {
                    MyLog.e(TAG, e);
                }
                switch (status) {
                    case COMPLETE:
                        MyLog.v(TAG, "sync complete");
                        break;
                    case NEWDATA:
                        MyLog.v(TAG, "more data");
                        continue;
                    case HTTPERRORS:
                    case INTERNAL_ERRORS:
                        MyLog.v(TAG, "retrying errored requests in 10secs");
                        sleep(10);
                        continue;
                    default:
                        MyLog.v(TAG, "why is this happening");
                }
            }
            sleep(2);
        }
    }

    private void sleep(int secs) {
        //running = false;
        MyUtil.sleepUninterrupted(secs * 1000);
        //running = true;
    }
}
