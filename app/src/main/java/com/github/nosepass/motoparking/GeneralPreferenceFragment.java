package com.github.nosepass.motoparking;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Catch all location for most settings.
 */
public class GeneralPreferenceFragment extends PreferenceFragment {
    //public static final String TAG = "GeneralPreferenceFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        MyUtil.bindPreferenceSummaryToValue(findPreference(PrefKeys.BASE_URL));
    }
}
