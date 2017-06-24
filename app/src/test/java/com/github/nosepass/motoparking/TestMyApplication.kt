package com.github.nosepass.motoparking

import android.annotation.SuppressLint
import android.app.Application
import android.os.StrictMode
import android.telephony.TelephonyManager
import android.text.TextUtils
import com.github.nosepass.motoparking.di.Dagger
import timber.log.Timber
import android.os.StrictMode.setVmPolicy
import android.os.StrictMode.setThreadPolicy


/**
 * Test Application that will be automatically picked up by Robolectric
 */
@SuppressLint("Registered")
class TestMyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Dagger.init(this)
    }
}
