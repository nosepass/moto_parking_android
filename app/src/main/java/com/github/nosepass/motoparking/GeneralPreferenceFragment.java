package com.github.nosepass.motoparking;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Catch all location for most settings.
 */
public class GeneralPreferenceFragment extends PreferenceFragment {
    public static final String TAG = "GeneralPreferenceFragment";
    private static final String OLD_URL_KEY = "old_url";
    private static final String OLD_PASSWORD_KEY = "old_password";
    private static final String DEV_MODE_KEY = "dev_mode";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        Preference dev = findPreference(DEV_MODE_KEY);

        MyUtil.bindPreferenceSummaryToValue(findPreference(PrefKeys.BASE_URL));
        MyUtil.bindPreferenceSummaryToValue(dev);

        dev.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateDevMode(newValue + "");
                return true;
            }
        });

        if (!BuildConfig.DEBUG) {
            getPreferenceScreen().removePreference(dev);
            getPreferenceScreen().removePreference(findPreference(PrefKeys.NICKNAME));
            getPreferenceScreen().removePreference(findPreference(PrefKeys.PASSWORD));
        }
    }

    private void updateDevMode(String newValue) {
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        String currenturl = prefs.getString(PrefKeys.BASE_URL, "http://");
        String currentpw = prefs.getString(PrefKeys.PASSWORD, "~0~");
        if ("Dev".equals(newValue)) {
            MyLog.v(TAG, "switching to dev url");
            prefs
                    .edit()
                    .putString(PrefKeys.BASE_URL, "http://192.168.2.141:3000/")
                    .putString(PrefKeys.PASSWORD, "8bf0810561612caf5c6cd023fed75f6e")
                    .putString(OLD_URL_KEY, currenturl)
                    .putString(OLD_PASSWORD_KEY, currentpw)
                    .apply();
        } else if ("Normal".equals(newValue)) {
            String oldurl = prefs.getString(OLD_URL_KEY, "");
            String oldpw = prefs.getString(OLD_PASSWORD_KEY, "");
            MyLog.v(TAG, "switching back to url " + oldurl);
            prefs
                    .edit()
                    .putString(PrefKeys.BASE_URL, oldurl)
                    .putString(PrefKeys.PASSWORD, oldpw)
                    .apply();
        }
        updateSummary(DEV_MODE_KEY, newValue); // so the new state of dev_mode hasn't been persisted yet
        updateSummary(PrefKeys.BASE_URL);
        updateSummary(PrefKeys.PASSWORD); // ok maybe password didn't have a summary, but it looks cool
    }

    private void updateSummary(String key, String value) {
        MyUtil.sBindPreferenceSummaryToValueListener.onPreferenceChange(findPreference(key), value);
    }

    private void updateSummary(String key) {
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        updateSummary(key, prefs.getString(key, "'"));
    }
}
