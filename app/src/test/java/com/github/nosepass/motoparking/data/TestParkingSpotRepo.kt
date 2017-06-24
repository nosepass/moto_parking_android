package com.github.nosepass.motoparking.data

import android.os.Build.VERSION_CODES.O
import com.github.nosepass.motoparking.BuildConfig
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.*
import java.util.concurrent.TimeUnit.SECONDS

@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(O), constants = BuildConfig::class)
class TestParkingSpotRepo {
    lateinit var subject: ParkingSpotRepo

    @Before
    @Throws(Exception::class)
    fun setUpMocks() {
        subject = ParkingSpotRepo(RuntimeEnvironment.application, "parking.db", false, true)
    }

    @Test
    @Throws(Exception::class)
    fun all() {
        createThree()
        subject.all()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue { l -> l.size == 3 }
    }

    @Test
    @Throws(Exception::class)
    fun replace() {
        createThree()
        assertRecordCount(3)

        val replacements = arrayListOf<ParkingSpot>(createSpot("1", "mod"), createSpot("3", "mod"))
        subject.replace(replacements)
                .test()
                .awaitDone(1, SECONDS)
                .assertComplete()

        subject.all()
                .test()
                .awaitCount(1)
                .awaitDone(2, SECONDS)
                .assertNoErrors()
                // expect middle value to not be there
                .assertValue { l -> l.size == 2 }
                .assertValue { l -> l.all{ it.name == "mod" } }
    }

    @Test
    fun replace_shouldRejectEmptyLists() {
        subject.replace(arrayListOf())
                .test()
                .assertError(IllegalStateException::class.java)
    }

    @Test
    fun replace_shouldHandle1000records() {
        val thousand = ArrayList<ParkingSpot>()
        for (i in 1..1000) {
            thousand.add(createSpot(i.toString()))
        }

        subject.replace(thousand)
                .test()
                .assertNoErrors()
        assertRecordCount(1000)
    }

    @Test
    @Throws(Exception::class)
    fun create() {
        assertRecordCount(0)

        subject.create(createSpot("1"))
                .test()
                .awaitCount(1)
                .assertValue { it.id == "1" }

        subject.all()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue { l -> l.size == 1 }
                .assertValue { l -> l.all { it.id == "1" } }
    }

    private fun createSpot(id: String, name: String = "original"): ParkingSpot {
        return ParkingSpot(id, name = name, createdAt = Date(), updatedAt = Date())
    }

    private fun createThree() {
        for (id in arrayOf("1", "2", "3")) {
            subject.create(createSpot(id))
                    .blockingFirst()
        }
    }

    private fun assertRecordCount(count: Int) {
        subject.all()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue { l -> l.size == count }
                .dispose()
    }
}
