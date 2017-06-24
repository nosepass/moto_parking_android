package com.github.nosepass.motoparking.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters

@Database(version = 4, entities = arrayOf(ParkingSpot::class))
@TypeConverters(DateConverter::class)
internal abstract class MyDb : RoomDatabase() {
    abstract fun parkingSpotDao(): ParkingSpotDao
}
