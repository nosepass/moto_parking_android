package com.github.nosepass.motoparking.data

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.TextUtils
import com.github.nosepass.motoparking.R

/**
 * Key/value settings
 */
class SettingsRepo(context: Context) {
    private val BASE_URL = "api_url"
    private val USER_ID = "user_id"
    private val NICK = "nickname"
    private val PASSWORD = "password"
    private val PARKING_DB_REV = "db_version"
    private val STARTING_LAT_LONG = "starting_loc"
    private val STARTING_ZOOM = "starting_zoom"
    private val CURRENT_POSITION = "current_pos"
    private val CURRENT_ZOOM = "current_zoom"

    private val prefs: SharedPreferences

    init {
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        migrate()
        PreferenceManager.setDefaultValues(context, R.xml.prefs, true)
    }

    var baseUrl: String
        get() = prefs.getString(BASE_URL, "")!! + ""
        set(v) = prefs.edit().putString(BASE_URL, v).apply()

    var userId: Long
        get() = prefs.getLong(USER_ID, -1)
        set(v) = prefs.edit().putLong(USER_ID, v).apply()

    var nickname: String
        get() = prefs.getString(NICK, "")!! + ""
        set(v) = prefs.edit().putString(NICK, v).apply()

    var password: String
        get() = prefs.getString(PASSWORD, "")!! + ""
        set(v) = prefs.edit().putString(PASSWORD, v).apply()

    var parkingDbRev: Long
        get() = prefs.getLong(PARKING_DB_REV, 0)
        set(v) = prefs.edit().putLong(PARKING_DB_REV, v).apply()

    private fun migrate() {
        // overwrite old api url since that server died
        val url = prefs.getString(BASE_URL, "")
        if (TextUtils.equals(url, "http://94.23.35.76:8080/")) {
            prefs.edit().remove(BASE_URL).apply()
        }
    }
}
