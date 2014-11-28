package com.github.nosepass.motoparking.http;

import android.content.Context;
import android.content.Intent;

import com.github.nosepass.motoparking.MyLog;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;

/**
 * Accumulates actions that get posted to the server periodically
 */
public class SyncQueue {
    private static final String TAG = "SyncQueue";
    private static final String CLSNAME = SyncQueue.class.getName();
    public static final String SYNC_DATA_PENDING = CLSNAME + ".SYNC_DATA_PENDING";
    public static final String SYNC_DATA_COMPLETE = CLSNAME + ".SYNC_DATA_COMPLETE";
    public static final String SYNC_UPLOAD_START = CLSNAME + ".SYNC_UPLOAD_START";
    public static final String SYNC_UPLOAD_STOP = CLSNAME + ".SYNC_UPLOAD_STOP";

    private Context context;
    private List<HttpAction> actions = new ArrayList<HttpAction>();

    enum Status {
        COMPLETE, LOGIN_FORBIDDEN, OTHER_FORBIDDEN, NETWORK_ERRORS, INTERNAL_ERRORS, NEWDATA
    }

    public SyncQueue(Context context) {
        this.context = context;
    }

    public Status doSync() {
        context.sendBroadcast(new Intent(SYNC_UPLOAD_START));
        boolean networkErrors = false;
        boolean httpCodeErrors = false;
        boolean parseErrors = false;
        boolean internalErrors = false;
        boolean loginForbidden = false;
        boolean otherForbidden = false;
        List<HttpAction> copy = new ArrayList<HttpAction>(actions); // avoid other threads pushing onto list
        List<HttpAction> completed = new ArrayList<HttpAction>();
        for (HttpAction act : copy) {
            boolean currentCompleted = false;
            try {
                MyLog.v(TAG, "executing http action " + act);
                act.attempts++;
                act.executeHttpRequest();
                currentCompleted = true;
                act.processResponse(context);
            } catch (RetrofitError e) {
                MyLog.e(TAG, e);
                switch (e.getKind()) {
                    case NETWORK:
                        networkErrors = true;
                        break;
                    case HTTP:
                        httpCodeErrors = true;
                    case CONVERSION:
                        parseErrors = true;
                        break;
                    case UNEXPECTED:
                        internalErrors = true;
                        break;
                }
                if (e.getResponse() != null && e.getResponse().getStatus() == 403) {
                    if (act.isLoginAction()) {
                        loginForbidden = true;
                    } else {
                        otherForbidden = true;
                    }
                } else if (httpCodeErrors || parseErrors || internalErrors) {
                    MyLog.v(TAG, "discarding action because of error - " + act);
                    currentCompleted = true;
                }
            } catch (Exception e) {
                internalErrors = true;
            }
            if (currentCompleted) {
                completed.add(act);
            }

            if (loginForbidden || otherForbidden) {
                // stop and (re-)attempt login
                break;
            }
//            if (Thread.interrupted()) {
//                // service is prolly shutting down
//                break;
//            }
        }

        context.sendBroadcast(new Intent(SYNC_UPLOAD_STOP));
        MyLog.v(TAG, "upload stop");
        synchronized (this) {
            actions.removeAll(completed);
            if (actions.size() == 0) {
                context.sendBroadcast(new Intent(SYNC_DATA_COMPLETE));
            }
        }

        if (loginForbidden) {
            return Status.LOGIN_FORBIDDEN;
        }
        if (otherForbidden) {
            return Status.OTHER_FORBIDDEN;
        }
        if (networkErrors) {
            return Status.NETWORK_ERRORS;
        }
        if (httpCodeErrors || parseErrors || internalErrors) {
            return Status.INTERNAL_ERRORS;
        }
        if (actions.size() == 0) {
            return Status.COMPLETE;
        }
        return Status.NEWDATA;
    }

    public synchronized void add(HttpAction action) {
        if (actions.size() == 0) {
            context.sendBroadcast(new Intent(SYNC_DATA_PENDING));
        }
        actions.add(action);
    }

    public synchronized void addToFront(HttpAction action) {
        if (actions.size() == 0) {
            context.sendBroadcast(new Intent(SYNC_DATA_PENDING));
        }
        actions.add(0, action);
    }

    public synchronized boolean hasActions() {
        return actions.size() > 0;
    }
}
