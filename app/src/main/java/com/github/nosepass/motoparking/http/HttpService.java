package com.github.nosepass.motoparking.http;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.github.nosepass.motoparking.MyLog;
import com.github.nosepass.motoparking.MyUtil;
import com.github.nosepass.motoparking.util.ForegroundManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Manages communication with server via a queue of http requests.
 */
public class HttpService extends Service {
    private static final String TAG = "HttpService";
    private static final String CLSNAME = HttpService.class.getName();
    /** Add an upload action to be carried out */
    public static final String ADD_HTTP_ACTION = CLSNAME + ".ADD_HTTP_ACTION";
    /** json form of HttpAction to be reinflated */
    public static final String EXTRA_SERIALIZED_ACTION = CLSNAME + ".EXTRA_SERIALIZED_ACTION";

    private SharedPreferences prefs;
    private ConnectivityManager cm;
    private BroadcastReceiver backgroundReceiver = new BackgroundReceiver();
    private BroadcastReceiver newActionReceiver = new NewActionReceiver();

    private Thread uploadThread;
    public static SyncQueue uploadQueue;
    private boolean started;

    /**
     * Add an http request to UploadService via an intent, so it can be executed on the sync thread.
     */
    public static void addSyncAction(Context c, HttpAction act) {
        Intent i = new Intent(HttpService.ADD_HTTP_ACTION);
        i.putExtra(HttpService.EXTRA_SERIALIZED_ACTION, act.toJson());
        c.sendBroadcast(i);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.v(TAG, "onCreate");
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cm = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        registerReceiver(backgroundReceiver, new IntentFilter(ForegroundManager.APP_IN_BACKGROUND));
        registerReceiver(newActionReceiver, new IntentFilter(ADD_HTTP_ACTION));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLog.v(TAG, "Received start id " + startId + ": " + intent);

        // currently the service is only launched via startService, so this is the entrypoint
        // it can be called multiple times since each activity calls startService in onResume
        if (!started) {
            startTheStuffYo();
            started = true;
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        MyLog.v(TAG, "onDestroy");
        super.onDestroy();
        stopTheStuffBro();
        unregisterReceiver(backgroundReceiver);
        unregisterReceiver(newActionReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        MyLog.v(TAG, "onBind");
        return new Binder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        MyLog.v(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    private void startTheStuffYo() {
        uploadThread = new Thread(new UploadRoutine(), "UploadService");
        uploadThread.start();
    }

    private void stopTheStuffBro() {
        uploadThread.interrupt();
    }

    private void uploadLoop() throws InterruptedException {
        while (!Thread.interrupted()) {
            boolean connected = MyUtil.isNetworkAvailable(cm);
            if (!connected && uploadQueue.hasActions()) {
                MyLog.v(TAG, "no network - trying again in 30secs");
                sleep(30);
                continue;
            }
            if (uploadQueue.hasActions()) {
                MyLog.v(TAG, "syncing actions");
                SyncQueue.Status status = SyncQueue.Status.INTERNAL_ERRORS;
                try {
                    status = uploadQueue.doSync();
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
                    case NETWORK_ERRORS:
                        MyLog.v(TAG, "retrying errored requests in 10secs");
                        sleep(10);
                    case INTERNAL_ERRORS:
                        MyLog.v(TAG, "retrying exceptioned requests in 30secs");
                        sleep(30);
                        continue;
                    case LOGIN_FORBIDDEN:
                        MyLog.v(TAG, "403 forbidden, user not authorized");
                        break;
                    case OTHER_FORBIDDEN:
                        MyLog.v(TAG, "403 forbidden, attempting login first, after a few sec");
                        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                        uploadQueue.addToFront(new Login(prefs, tm));
                        sleep(5); // ends up being 7sec total
                        break;

                    default:
                        MyLog.v(TAG, "why is this happening");
                }
            }
            sleep(2);
        }
    }

    private void sleep(int secs) throws InterruptedException {
        if (!Thread.interrupted()) {
            Thread.sleep(secs * 1000);
        }
    }

    /**
     * Stops service when all activities are in background.
     */
    private class BackgroundReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MyLog.v(TAG, "stopping service since we're in background");
            stopSelf();
        }
    }

    /**
     * Receives new actions to queue up for upload
     */
    private class NewActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MyLog.v(TAG, "got action add intent");
            try {
                String serialized = intent.getStringExtra(EXTRA_SERIALIZED_ACTION);
                JsonObject json = new JsonParser().parse(serialized).getAsJsonObject();
                String clazz = json.get("class").getAsString();
                HttpAction act;
                if (Login.class.getName().equals(clazz)) {
                    // I need to remove the SharedPreferences requirement from login
                    // TODO
                    Object o = Class.forName(clazz)
                            .getConstructor(SharedPreferences.class, JsonObject.class)
                            .newInstance(prefs, json);
                    act = (HttpAction) o;
                } else {
                    Object o = Class.forName(clazz).getConstructor(JsonObject.class).newInstance(json);
                    act = (HttpAction) o;
                }
                MyLog.v(TAG, "adding action " + act);
                uploadQueue.add(act);
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        }
    }

    private class UploadRoutine implements Runnable {
        private static final String TAG = "HttpService.UploadRoutine";
        @Override
        public void run() {
            long id = Thread.currentThread().getId();
            MyLog.v(TAG, "thread %s started", id);
            try {
                uploadLoop();
            } catch (InterruptedException e) {
                MyLog.v(TAG, e + "");
                // allow thread to exit
            }
            MyLog.v(TAG, "thread %s exiting", id);
        }
    }

}
