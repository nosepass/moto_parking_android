package com.github.nosepass.motoparking;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.github.nosepass.motoparking.db.ParkingSpot;


public abstract class BaseSpotActivity extends BaseAppCompatActivity
        implements EditParkingSpotFragment.OnSaveListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_spot);

        if (savedInstanceState == null) {
            EditParkingSpotFragment f = new EditParkingSpotFragment();
            f.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction()
                    .add(R.id.container, f)
                    .commit();
        }

        setSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public abstract void onParkingSpotSaved(ParkingSpot spot);
}
