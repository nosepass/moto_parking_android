package com.github.nosepass.motoparking.http;

import android.content.Context;
import android.content.Intent;

import com.github.nosepass.motoparking.MyLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Accumulates actions that get posted to the server periodically
 */
public class SyncQueue {
    private static final String TAG = "http.SyncQueue";
    private static final String CLSNAME = SyncQueue.class.getName();
    public static final String SYNC_DATA_PENDING = CLSNAME + ".SYNC_DATA_PENDING";
    public static final String SYNC_DATA_COMPLETE = CLSNAME + ".SYNC_DATA_COMPLETE";
    public static final String SYNC_UPLOAD_START = CLSNAME + ".SYNC_UPLOAD_START";
    public static final String SYNC_UPLOAD_STOP = CLSNAME + ".SYNC_UPLOAD_STOP";
//    public static final String SYNC_DOWNLOAD_START = CLSNAME + ".SYNC_DOWNLOAD_START";
//    public static final String SYNC_DOWNLOAD_STOP = CLSNAME + ".SYNC_DOWNLOAD_STOP";
    public static final String FORBIDDEN = CLSNAME + ".FORBIDDEN";

    private Context context;
    private List<HttpAction> actions = new ArrayList<HttpAction>();

    enum Status {
        COMPLETE, HTTPERRORS, INTERNAL_ERRORS, NEWDATA
    }

    public SyncQueue(Context context) {
        this.context = context;
        //prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public Status doSync() {
        context.sendBroadcast(new Intent(SYNC_UPLOAD_START));
        boolean httpErrors = false;
        boolean exceptions = false;
        boolean badResult = false;
        List<HttpAction> copy = new ArrayList<HttpAction>(actions); // avoid other threads pushing onto list
        List<HttpAction> completed = new ArrayList<HttpAction>();
        for (HttpAction act : copy) {
            boolean currentCompleted = false;
            try {
                MyLog.v(TAG, "executing http action " + act.getClass().getSimpleName());
                act.attempts++;
                act.executeHttpRequest();
                if (act.getStatusCode() == 200) {
                    try {
                        MyLog.v(TAG, "parsing http response");
                        act.parseResult();
                        currentCompleted = true;
                    } catch (Exception e) {
                        MyLog.e(TAG, e);
                        badResult = true;
                    }
                } else {
                    MyLog.v(TAG, "error status " + act.getStatusCode());
                    httpErrors = true;
                    if (act.getStatusCode() == 403) {
                        context.sendBroadcast(new Intent(FORBIDDEN));
                    }
                }
            } catch (Exception e) {
                MyLog.e(TAG, e);
                exceptions = true;
            }
            if (currentCompleted || act.attempts >= 3) {
                completed.add(act);
            }
        }
        context.sendBroadcast(new Intent(SYNC_UPLOAD_STOP));
        MyLog.v(TAG, "upload stop");
        synchronized (this) {
            actions.removeAll(completed);
            if (actions.size() == 0) {
                context.sendBroadcast(new Intent(SYNC_DATA_COMPLETE));
            }
        }
        if (exceptions) return Status.INTERNAL_ERRORS;
        if (httpErrors || badResult) return Status.HTTPERRORS;
        if (actions.size() == 0) return Status.COMPLETE;
        return Status.NEWDATA;
    }

    public synchronized void add(HttpAction action) {
        if (actions.size() == 0) {
            context.sendBroadcast(new Intent(SYNC_DATA_PENDING));
        }
        actions.add(action);
    }

    public synchronized boolean hasActions() {
        return actions.size() > 0;
    }
}
