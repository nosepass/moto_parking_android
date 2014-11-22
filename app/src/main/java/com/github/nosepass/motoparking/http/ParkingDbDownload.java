package com.github.nosepass.motoparking.http;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.github.nosepass.motoparking.MotoParkingApplication;
import com.github.nosepass.motoparking.MyLog;
import com.github.nosepass.motoparking.db.DaoMaster;
import com.github.nosepass.motoparking.db.DaoSession;
import com.github.nosepass.motoparking.db.MySQLiteOpenHelper;
import com.github.nosepass.motoparking.db.ParkingSpot;
import com.github.nosepass.motoparking.db.ParkingSpotDao;

import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Retrieve a list of relevant parking spots from the server to store locally.
 */
public class ParkingDbDownload extends JSONArrayAction {
    private static final String TAG = "http.ParkingDbDownload";

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
    }
    public static void dropDbsForDebug() {
        // recreate tables for debug purposes, since I don't feel like migrating during dev
        MyLog.v(TAG, "dropping and recreating tables");
        SQLiteDatabase db = daoMaster.getDatabase();
        ParkingSpotDao.dropTable(db, true);
        ParkingSpotDao.createTable(db, false);
    }

    /**
     *
     * @param lat current user's latitude
     * @param longitude current user's longitude
     */
    public ParkingDbDownload(SharedPreferences prefs, double lat, double longitude) {
        super(prefs);
        buildParams("latitude", lat + "", "longitude", longitude + "");
    }

    public void executeHttpRequest() {
        // hacktastic
        try {
            MyLog.v(TAG, "downloading stuff");
            ParkingSpotApi api = MotoParkingApplication.parkingSpotApi;
            if (api != null) {
                List<ParkingSpot> spots = api.getSpots();
                saveSpotsToDb(spots);
            } else {
                throw new Exception("no retrofit api found!");
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
            errors = true;
            //exception = e;
        }
    }

    @Override
    protected HttpUriRequest createRequest() {
        String url = baseUrl +  "/parking_spots.json";
        return createHttpGetWithQueryParams(url);
    }

    protected boolean retryJsonParseErrors() {
        return true;
    }

    private void saveSpotsToDb(List<ParkingSpot> spots) {

        DaoSession s = daoMaster.newSession();
        //s.getParkingSpotDao().deleteAll();
        dropDbsForDebug();
        s.getParkingSpotDao().insertInTx(spots);
    }
}
