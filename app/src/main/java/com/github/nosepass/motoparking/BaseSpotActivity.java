package com.github.nosepass.motoparking;

import android.os.Bundle;
import android.view.MenuItem;

import com.github.nosepass.motoparking.db.ParcelableParkingSpot;
import com.github.nosepass.motoparking.db.ParkingSpot;

/**
 * Takes care of managing the EditSpotFragment and other sundries,
 * for Create and Edit Spot.
 */
public abstract class BaseSpotActivity extends BaseAppCompatActivity
        implements EditParkingSpotFragment.OnSaveListener {

    private static final String CLSNAME = BaseSpotActivity.class.getName();
    public static final String EXTRA_SPOT = CLSNAME + ".EXTRA_SPOT";

    protected EditParkingSpotFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_spot);

        fragment = (EditParkingSpotFragment) getFragmentManager().findFragmentById(R.id.edit_fragment);
        fragment.setSpot(getIntent().<ParcelableParkingSpot>getParcelableExtra(EXTRA_SPOT));

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
