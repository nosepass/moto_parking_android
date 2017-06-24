package com.github.nosepass.motoparking

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.text.TextUtils
import android.util.Log
import com.bugsnag.android.Bugsnag
import com.github.nosepass.motoparking.data.SettingsRepo
import com.github.nosepass.motoparking.di.AppComponent
import com.github.nosepass.motoparking.di.DaggerAppComponent
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject


class MyApplication : Application() {
    private lateinit var appComponent: AppComponent
    @Inject
    internal lateinit var settings: SettingsRepo

    override fun onCreate() {
        super.onCreate()
        Bugsnag.init(this)
        Timber.plant(CrashReporterTree())
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            //AndroidDevMetrics.initWith(this)
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
        }

        appComponent = DaggerAppComponent.builder()
                .application(this)
                .build()
        appComponent.inject(this)

        Completable.fromAction {
            val userid = settings.userId
            val nick = settings.nickname
            if (!TextUtils.isEmpty(nick)) {
                Bugsnag.setUser(userid.toString(), null, nick)
            }
            Bugsnag.addToTab("Device", "Device ID", Build.ID)
        }
                .subscribeOn(Schedulers.io())
                .subscribe({}, Timber::e)
    }

    // map Timber.e/i to Bugsnag's handled exception notifier
    class CrashReporterTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
            Bugsnag.leaveBreadcrumb("$tag $message")
            if (priority >= Log.INFO && t != null) {
                Bugsnag.notify(t)
            } else if (priority >= Log.ERROR && t == null && message != null) {
                Bugsnag.notify(Exception(message))
            }
        }

    }
}
