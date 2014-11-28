package com.github.nosepass.motoparking.http;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.github.nosepass.motoparking.MotoParkingApplication;
import com.github.nosepass.motoparking.MyLog;
import com.github.nosepass.motoparking.db.DaoMaster;
import com.github.nosepass.motoparking.db.DaoSession;
import com.github.nosepass.motoparking.db.MySQLiteOpenHelper;
import com.github.nosepass.motoparking.db.ParkingSpot;
import com.github.nosepass.motoparking.db.ParkingSpotDao;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Retrieve a list of relevant parking spots from the server to store locally.
 */
public class ParkingDbDownload extends HttpAction {
    private static final String TAG = "http.ParkingDbDownload";
    private static final String CLSNAME = ParkingDbDownload.class.getName();
    public static final String DOWNLOAD_COMPLETE = CLSNAME + ".DOWNLOAD_COMPLETE";

    private List<ParkingSpot> response;

    // also hacktastic. TODO remove
    public static MySQLiteOpenHelper sqliteOpenHelper;
    public static DaoMaster daoMaster;
    public static synchronized void initDb(Context c) {
        if (sqliteOpenHelper == null) {
            sqliteOpenHelper = new MySQLiteOpenHelper(c);
            try {
                SQLiteDatabase db = sqliteOpenHelper.getWritableDatabase();
                daoMaster = new DaoMaster(db);
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        }
        ParkingDbDownload.dropDbsForDebug();
    }
    public static void dropDbsForDebug() {
        // recreate tables for debug purposes, since I don't feel like migrating during dev
        MyLog.v(TAG, "dropping and recreating tables");
        SQLiteDatabase db = daoMaster.getDatabase();
        ParkingSpotDao.dropTable(db, true);
        ParkingSpotDao.createTable(db, false);
    }

    /**
     * @param lat current user's latitude
     * @param longitude current user's longitude
     */
    public ParkingDbDownload(double lat, double longitude) {
    }

    public ParkingDbDownload(JsonObject serialized) {
    }

    public void executeHttpRequest() {
        MyLog.v(TAG, "downloading stuff");
        ParkingSpotApi api = MotoParkingApplication.parkingSpotApi;
        response = api.getSpots();
    }


    @Override
    public void processResponse(Context c) {
        saveSpotsToDb(response);
        c.sendBroadcast(new Intent(DOWNLOAD_COMPLETE));
    }

    private void saveSpotsToDb(List<ParkingSpot> spots) {

        DaoSession s = daoMaster.newSession();
        s.getParkingSpotDao().deleteAll();
        //dropDbsForDebug();
        s.getParkingSpotDao().insertInTx(spots);
    }
}
