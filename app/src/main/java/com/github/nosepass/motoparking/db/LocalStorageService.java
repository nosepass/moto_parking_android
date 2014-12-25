package com.github.nosepass.motoparking.db;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;

import com.github.nosepass.motoparking.MyLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs sql to fetch local data in a background thread.
 */
public class LocalStorageService extends IntentService {
    private static final String TAG = "LocalStorageService";

    private static final String CLSNAME = LocalStorageService.class.getName();

    /** the id of the request, in case multiple of the same type have been queued */
    private static final String EXTRA_REQUEST_ID = CLSNAME + ".EXTRA_REQUEST_ID";
    /** A parcelable list of ParkingSpots to be saved or that have been loaded */
    private static final String EXTRA_SPOTS = CLSNAME + ".EXTRA_SPOTS";
    /** A single parcelable ParkingSpot to be saved or deleted */
    public static final String EXTRA_SPOT = CLSNAME + ".EXTRA_SPOTS";
    /** A request errored out for some reason */
    private static final String REQUEST_FAILED  = CLSNAME + ".REQUEST_FAILED";

    /** Create or open the sqlite db */
    private static final String INIT_DB_REQ = CLSNAME + ".INIT_DB_REQ";
    /** Save downloaded parking spots in EXTRA_SPOTS to db */
    private static final String SAVE_SPOTS_REQ  = CLSNAME + ".SAVE_SPOTS_REQ";
    /** intent sent out when spots have been saved */
    public static final String SAVE_SPOTS_COMPLETE  = CLSNAME + ".SAVE_SPOTS_COMPLETE";

    /** Load parking spots from db into memory */
    private static final String LOAD_SPOTS_REQ  = CLSNAME + ".LOAD_SPOTS_REQ";
    /** intent sent out when spots are loaded, with EXTRA_SPOTS */
    private static final String LOAD_SPOTS_COMPLETE  = CLSNAME + ".LOAD_SPOTS_COMPLETE";

    /** Insert a parking spot in EXTRA_SPOT into the db */
    private static final String INSERT_SPOT_REQ  = CLSNAME + ".INSERT_SPOT_REQ";
    public static final String INSERT_SPOT_COMPLETE  = CLSNAME + ".INSERT_SPOT_COMPLETE";
    /** Update the parking spot in EXTRA_SPOT in the db */
    private static final String UPDATE_SPOT_REQ  = CLSNAME + ".UPDATE_SPOT_REQ";
    public static final String UPDATE_SPOT_COMPLETE  = CLSNAME + ".UPDATE_SPOT_COMPLETE";
    /** Delete the parking spot in EXTRA_SPOT from the db */
    private static final String DELETE_SPOT_REQ  = CLSNAME + ".DELETE_SPOT_REQ";
    public static final String DELETE_SPOT_COMPLETE  = CLSNAME + ".DELETE_SPOT_COMPLETE";
    /** Refresh the data for the spot in EXTRA_SPOT from the db */
    private static final String REFRESH_SPOT_REQ  = CLSNAME + ".REFRESH_SPOT_REQ";
    private static final String REFRESH_SPOT_COMPLETE  = CLSNAME + ".REFRESH_SPOT_COMPLETE";

    public static MySQLiteOpenHelper sqliteOpenHelper;
    public static DaoMaster daoMaster;
    private static volatile int requestId = 0;

    public static void sendInitDb(Context c) {
        c.startService(createRequestIntent(c, INIT_DB_REQ));
    }

    public static void sendSaveSpots(Context c, ArrayList<ParcelableParkingSpot> spots) {
        Intent i = createRequestIntent(c, SAVE_SPOTS_REQ);
        i.putParcelableArrayListExtra(EXTRA_SPOTS, spots);
        c.startService(i);
    }

    public static void sendLoadSpots(Context c, final Callback<List<? extends ParkingSpot>> callback) {
        Intent i = createRequestIntent(c, LOAD_SPOTS_REQ);
        int reqId = i.getIntExtra(EXTRA_REQUEST_ID, -1);

        registerTempReceiver(c, reqId, new Callback<Intent>() {
            public void onSuccess(Intent result) {
                List<ParcelableParkingSpot> spots = result.getParcelableArrayListExtra(EXTRA_SPOTS);
                callback.onSuccess(spots);
            }

            public void onError() {
                callback.onError();
            }
        }, LOAD_SPOTS_COMPLETE);

        c.startService(i);
    }

    public static void sendInsertSpot(Context c, ParcelableParkingSpot spot, final Callback<ParcelableParkingSpot> callback) {
        Intent i = createRequestIntent(c, INSERT_SPOT_REQ, spot);
        int reqId = i.getIntExtra(EXTRA_REQUEST_ID, -1);

        registerTempReceiver(c, reqId, new Callback<Intent>() {
            public void onSuccess(Intent result) {
                ParcelableParkingSpot spot = result.getParcelableExtra(EXTRA_SPOT);
                callback.onSuccess(spot);
            }

            public void onError() {
                callback.onError();
            }
        }, INSERT_SPOT_COMPLETE);

        c.startService(i);
    }

    public static void sendUpdateSpot(Context c, ParkingSpot spot) {
        c.startService(createRequestIntent(c, UPDATE_SPOT_REQ, spot));
    }

    public static void sendDeleteSpot(Context c, ParkingSpot spot) {
        c.startService(createRequestIntent(c, DELETE_SPOT_REQ, spot));
    }

    public static void sendRefreshSpot(Context c, ParcelableParkingSpot spot, final Callback<ParcelableParkingSpot> callback) {
        Intent i = createRequestIntent(c, REFRESH_SPOT_REQ, spot);
        int reqId = i.getIntExtra(EXTRA_REQUEST_ID, -1);

        registerTempReceiver(c, reqId, new Callback<Intent>() {
            public void onSuccess(Intent result) {
                ParcelableParkingSpot spot = result.getParcelableExtra(EXTRA_SPOT);
                callback.onSuccess(spot);
            }

            public void onError() {
                callback.onError();
            }
        }, REFRESH_SPOT_COMPLETE);

        c.startService(i);
    }

    private static synchronized Intent createRequestIntent(Context c, String action) {
        int reqId = requestId++;
        Intent i = new Intent(c, LocalStorageService.class);
        i.setAction(action);
        i.putExtra(EXTRA_REQUEST_ID, reqId);
        MyLog.v(TAG, "created request id %s act %s", reqId, action.replace(CLSNAME, ""));
        return i;
    }

    private static Intent createRequestIntent(Context c, String action, ParkingSpot spot) {
        Intent i = createRequestIntent(c, action);
        i.putExtra(EXTRA_SPOT, new ParcelableParkingSpot(spot));
        return i;
    }

    private static Intent createReplyIntent(String action, int reqId) {
        Intent i = new Intent(action);
        i.putExtra(EXTRA_REQUEST_ID, reqId);
        return i;
    }


    /**
     * Waits for the result of a request to get sent to a BroadcastReceiver,
     * then calls a callback with the result of the receiver and unregisters it.
     */
    private static void registerTempReceiver(final Context c, final int reqId,
                                             final Callback<Intent> cb,
                                             final String successAction) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(successAction);
        filter.addAction(REQUEST_FAILED);
        c.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent i) {
                String act = i.getAction();
                int currentReqId = i.getIntExtra(EXTRA_REQUEST_ID, -2);
                if (reqId == currentReqId) {
                    try {
                        if (successAction.equals(act)) {
                            cb.onSuccess(i);
                        } else if (REQUEST_FAILED.equals(act)) {
                            cb.onError();
                        }
                    } catch (Exception e) {
                        MyLog.e(TAG, e);
                    }
                    unregisterSelf(c);
                }
            }
            private void unregisterSelf(Context c) {
                try {
                    c.unregisterReceiver(this);
                } catch (IllegalArgumentException e) {
                    MyLog.e(TAG, e);
                }
            }
        }, filter);
    }

    public LocalStorageService() {
        super(TAG);
        setIntentRedelivery(true);
    }

    @Override
    public void onCreate() {
        MyLog.v(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        String act = i.getAction();
        int reqId = i.getIntExtra(EXTRA_REQUEST_ID, -1);
        MyLog.v(TAG, "queuing request id %s act %s", reqId, act.replace(CLSNAME, ""));
        return super.onStartCommand(i, flags, startId);
    }

    @Override
    public void onDestroy() {
        MyLog.v(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent i) {
        String act = i.getAction();
        int reqId = i.getIntExtra(EXTRA_REQUEST_ID, -1);
        MyLog.v(TAG, "processing request id %s act %s", reqId, act.replace(CLSNAME, ""));
        long start = System.currentTimeMillis();
        try {
            if (INIT_DB_REQ.equals(act)) {
                initDb();
            } else if (SAVE_SPOTS_REQ.equals(act)) {
                saveParkingSpots(i);
            } else if (LOAD_SPOTS_REQ.equals(act)) {
                loadSpots(reqId);
            } else if (INSERT_SPOT_REQ.equals(act)) {
                insertSpot(getSpot(i), reqId);
            } else if (UPDATE_SPOT_REQ.equals(act)) {
                updateSpot(getSpot(i), reqId);
            } else if (DELETE_SPOT_REQ.equals(act)) {
                deleteSpot(getSpot(i), reqId);
            } else if (REFRESH_SPOT_REQ.equals(act)) {
                refreshSpot(getSpot(i), reqId);
            } else {
                MyLog.e(TAG, "unknown action - " + act);
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
            Intent fail = new Intent(REQUEST_FAILED);
            fail.putExtra(EXTRA_REQUEST_ID, reqId);
            sendBroadcast(fail);
        }
        MyLog.v(TAG, "request id %s completed in %sms", reqId, System.currentTimeMillis() - start);
    }

    private void initDb() {
        if (sqliteOpenHelper == null) {
            sqliteOpenHelper = new MySQLiteOpenHelper(this);
            try {
                SQLiteDatabase db = sqliteOpenHelper.getWritableDatabase();
                daoMaster = new DaoMaster(db);
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        }
        dropDbsForDebug();
    }

    private void dropDbsForDebug() {
        // recreate tables for debug purposes, since I don't feel like migrating during dev
        MyLog.v(TAG, "dropping and recreating tables");
        SQLiteDatabase db = daoMaster.getDatabase();
        ParkingSpotDao.dropTable(db, true);
        ParkingSpotDao.createTable(db, false);
    }

    @SuppressWarnings("unchecked")
    private void saveParkingSpots(Intent i) {
        List<? extends ParkingSpot> spots =
                i.<ParcelableParkingSpot>getParcelableArrayListExtra(EXTRA_SPOTS);
        int reqId = i.getIntExtra(EXTRA_REQUEST_ID, -3);
        DaoSession s = daoMaster.newSession();
        s.getParkingSpotDao().deleteAll();
        //dropDbsForDebug();
        s.getParkingSpotDao().insertInTx((List<ParkingSpot>) spots);
        sendBroadcast(createReplyIntent(SAVE_SPOTS_COMPLETE, reqId));
    }

    private void loadSpots(int reqId) {
        ArrayList<ParcelableParkingSpot> spots = new ArrayList<>(); // this is an annoying extra step
        for (ParkingSpot spot : daoMaster.newSession().getParkingSpotDao().loadAll()) {
            spots.add(new ParcelableParkingSpot(spot));
        }
        Intent i = createReplyIntent(LOAD_SPOTS_COMPLETE, reqId);
        i.putParcelableArrayListExtra(EXTRA_SPOTS, spots);
        sendBroadcast(i);
    }

    private void insertSpot(ParcelableParkingSpot spot, int reqId) {
        daoMaster.newSession().getParkingSpotDao().insert(spot);
        sendSpotComplete(INSERT_SPOT_COMPLETE, spot, reqId);
    }

    private void updateSpot(ParcelableParkingSpot spot, int reqId) {
        daoMaster.newSession().getParkingSpotDao().update(spot);
        sendSpotComplete(UPDATE_SPOT_COMPLETE, spot, reqId);
    }

    private void deleteSpot(ParcelableParkingSpot spot, int reqId) {
        daoMaster.newSession().getParkingSpotDao().delete(spot);
        sendSpotComplete(DELETE_SPOT_COMPLETE, spot, reqId);
    }

    private void refreshSpot(ParcelableParkingSpot spot, int reqId) {
        daoMaster.newSession().getParkingSpotDao().refresh(spot);
        sendSpotComplete(REFRESH_SPOT_COMPLETE, spot, reqId);
    }

    private ParcelableParkingSpot getSpot(Intent i) {
        return i.getParcelableExtra(EXTRA_SPOT);
    }

    private void sendSpotComplete(String action, ParcelableParkingSpot spot, int reqId) {
        Intent i = createReplyIntent(action, reqId);
        i.putExtra(EXTRA_SPOT, spot);
        sendBroadcast(i);
    }

    public static class Callback<T> {
        public void onSuccess(T result) {}
        public void onError() {}
    }
}
