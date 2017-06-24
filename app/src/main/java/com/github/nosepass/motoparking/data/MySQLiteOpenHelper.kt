package com.github.nosepass.motoparking.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper

import timber.log.Timber

/**
 * Handle initial creation and opening of the sqlite tables.
 * An initial version of the database is copied from the assets folder.
 */
class MySQLiteOpenHelper(context: Context) : SQLiteAssetHelper(context, "parking.db", null, 4) {

    init {
        setForcedUpgrade() // looks like this is how all migrations will work, by copying a migrated db from assets
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Timber.d("onUpgrade %s -> %s", oldVersion, newVersion)
        // Have SQLiteAssetHelper handle the migration, either via .sql script or by copying entire db over
        super.onUpgrade(db, oldVersion, newVersion)
    }
}
