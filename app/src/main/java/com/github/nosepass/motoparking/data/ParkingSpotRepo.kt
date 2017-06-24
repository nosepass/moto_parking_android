package com.github.nosepass.motoparking.data

import android.arch.persistence.room.Room
import android.arch.persistence.room.RxRoom
import android.content.Context
import io.reactivex.Completable
import io.reactivex.Flowable
import okio.Okio
import java.io.FileOutputStream
import java.util.concurrent.Callable


/**
 * Wrapper around storing parking spots in sqlite
 */
class ParkingSpotRepo(
        val context: Context,
        val dbName: String,
        val copyFromAssets: Boolean = true,
        val allowMainThread: Boolean = false
) {
    // lazy-initializing db
    private val db = Flowable.fromCallable(initDb())
            .replay().autoConnect()
    private val dao = db.map { it.parkingSpotDao() }
    private val trigger = db.flatMap {
                if (dbName.isNotEmpty()) RxRoom.createFlowable(it, PARKING_SPOT_TABLE_NAME)
                else RxRoom.createFlowable(it)
            }

    private fun initDb(): Callable<MyDb> {
        return Callable {
            val dbFile = context.getDatabasePath(dbName)
            if (!dbFile.exists() && copyFromAssets) {
                // copy from assets
                context.getAssets().open("databases/" + dbName).use { from ->
                    FileOutputStream(dbFile).use { to ->
                        Okio.buffer(Okio.sink(to)).writeAll(Okio.source(from))
                    }
                }
            }

            val db: MyDb
            if (dbName.isNotEmpty()) {
                val builder = Room.databaseBuilder(context.applicationContext, MyDb::class.java, dbName)
                if (allowMainThread) builder.allowMainThreadQueries()
                db = builder.build()
            } else {
                db = Room.inMemoryDatabaseBuilder(context.applicationContext, MyDb::class.java).build()
            }
            db
        }
    }

    fun all(): Flowable<List<ParkingSpot>> = trigger
            .flatMap{ dao }
            .flatMap{ it.all() }

    fun replace(spots: List<ParkingSpot>): Completable {
        if (spots.isEmpty()) {
            return Completable.error(IllegalStateException("no spots, don't want to erase local ones"))
        }
        return db.flatMapCompletable { Completable.fromAction {
            it.runInTransaction {
                val dao = it.parkingSpotDao()
                dao.deleteAll()
                dao.insert(spots)
            }
        }}
    }

    fun create(spot: ParkingSpot): Flowable<ParkingSpot> = dao
                .flatMap { dao ->
                    Flowable.fromCallable { dao.insert(spot) }
                            .flatMap { dao.byRowid(it) }
                }

    fun update(spot: ParkingSpot): Completable = dao
            .map { it.update(spot) }
            .doOnNext(this::throwIfCountIncorrect)
            .ignoreElements()

    fun delete(spot: ParkingSpot): Completable = dao
            .map { it.delete(spot) }
            .doOnNext(this::throwIfCountIncorrect)
            .ignoreElements()

    private fun <T> completable(daoFunc: (T) -> Unit, arg: T): Completable {
        return Completable.fromAction{ daoFunc.invoke(arg) }
    }

    private fun throwIfCountIncorrect(count: Int) {
        if (count != 1) {
            throw NotUpdatedException("Number of rows was %s instead of 1", count)
        }
    }

    /**
     * Thrown when an unexpected number of rows are updated (usually 0 instead of 1)
     */
    class NotUpdatedException(message: String, vararg args: Any) : Exception(String.format(message, *args))
}
