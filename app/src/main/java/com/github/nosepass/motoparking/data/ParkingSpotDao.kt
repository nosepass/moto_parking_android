package com.github.nosepass.motoparking.data

import android.arch.persistence.room.*
import io.reactivex.Flowable

@Dao
internal abstract class ParkingSpotDao {
    val tableName = ""

    @Query("SELECT * FROM ParkingSpot")
    abstract fun all(): Flowable<List<ParkingSpot>>

    @Query("SELECT * FROM ParkingSpot where rowid = :rowid")
    abstract fun byRowid(rowid: Long): Flowable<ParkingSpot>

    @Query("DELETE FROM ParkingSpot")
    abstract fun deleteAll(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(spots: List<ParkingSpot>)

    /**
     * @return rowid of new row
     */
    @Insert
    abstract fun insert(spot: ParkingSpot): Long

    /**
     * @return number of rows modified
     */
    @Update
    abstract fun update(spot: ParkingSpot): Int

    /**
     * @return number of rows modified
     */
    @Delete
    abstract fun delete(spot: ParkingSpot): Int
}
