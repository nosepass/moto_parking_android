package com.github.nosepass.motoparking.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.nosepass.motoparking.MyLog;

/**
 * Handle initial creation and opening of the sqlite tables
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "db.MySQLiteOpenHelper";
    private static final String DATABASE_NAME = "parking.db";
    private static final int DATABASE_VERSION = 1;

    public MySQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        MyLog.v(TAG, "onCreate");
        ParkingSpotDao.createTable(db, false);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MyLog.v(TAG, "onUpgrade %s -> %s", oldVersion, newVersion);
        // do migration work
        ParkingSpotDao.dropTable(db, true);
        onCreate(db);
    }
}
