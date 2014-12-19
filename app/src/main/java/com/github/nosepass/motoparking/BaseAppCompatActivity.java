package com.github.nosepass.motoparking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.github.nosepass.motoparking.http.HttpService;


/**
 * This is the base of all activities.
 */
public class BaseAppCompatActivity extends ActionBarActivity {

    protected SharedPreferences prefs;

    protected Toolbar toolbar;
    protected boolean activityActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    protected void setSupportActionBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityActive = true;

        // The services stop themselves after 1sec if no activites remain in
        // the foreground.
        startService(new Intent(this, HttpService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityActive = false;
    }
}
