package com.github.nosepass.motoparking.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.nosepass.motoparking.MyLog;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Handle initial creation and opening of the sqlite tables.
 * An initial version of the database is copied from the assets folder.
 */
public class MySQLiteOpenHelper extends SQLiteAssetHelper {
    private static final String TAG = "MySQLiteOpenHelper";
    private static final String DATABASE_NAME = "parking.db";
    private static final int DATABASE_VERSION = 2;

    public MySQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade(2); // have SQLiteAssetHelper overwrite the old integer-pk table
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MyLog.v(TAG, "onUpgrade %s -> %s", oldVersion, newVersion);
        super.onUpgrade(db, oldVersion, newVersion);
    }
}
