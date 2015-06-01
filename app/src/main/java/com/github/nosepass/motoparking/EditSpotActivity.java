package com.github.nosepass.motoparking;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import com.github.nosepass.motoparking.db.LocalStorageService;
import com.github.nosepass.motoparking.db.ParcelableParkingSpot;
import com.github.nosepass.motoparking.db.ParkingSpot;
import com.github.nosepass.motoparking.http.DeleteSpot;
import com.github.nosepass.motoparking.http.EditSpot;
import com.github.nosepass.motoparking.http.HttpService;
import com.google.android.gms.maps.model.LatLng;

public class EditSpotActivity extends BaseSpotActivity
        implements EditParkingSpotFragment.OnDeleteListener,
        EditParkingSpotFragment.OnMoveListener {
    private static final String TAG = "EditSpotActivity";

    private ParcelableParkingSpot spot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment.getArguments().putBoolean(EditParkingSpotFragment.EXTRA_SHOW_EDIT_CONTROLS, true);
        spot = getIntent().getParcelableExtra(EXTRA_SPOT);
    }

    @Override
    public void onParkingSpotSaved(ParkingSpot spot) {
        finish();
        saveSpot(spot);
    }

    @Override
    public void onParkingSpotDeleted(ParkingSpot spot) {
        finish();
        LocalStorageService.sendDeleteSpot(this, spot);
        HttpService.addSyncAction(this, new DeleteSpot(spot));
    }

    @Override
    public void onParkingSpotMove(ParkingSpot spot) {
        Intent i = new Intent(this, CrosshairsActivity.class);
        i.putExtra(CrosshairsActivity.EXTRA_MAP_CENTER,
                new LatLng(spot.getLatitude(), spot.getLongitude()));
        i.putExtra(CrosshairsActivity.EXTRA_RETURN_LOC, true);
        i.putExtra(CrosshairsActivity.EXTRA_TITLE, getString(R.string.title_activity_crosshairs_move));
        startActivityForResult(i, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        MyLog.v(TAG, "onActivityResult");
        if (resultCode == RESULT_OK) {
            LatLng ll = data.getParcelableExtra(CrosshairsActivity.EXTRA_SELECTED_LOCATION);
            spot.setLatitude(ll.latitude);
            spot.setLongitude(ll.longitude);
            saveSpot(spot);
            showSnack(R.string.edit_spot_move_success);
        } else {
            showSnack(R.string.edit_spot_move_cancel);
        }
    }

    private void showSnack(int msgResId) {
        Snackbar snack = Snackbar.make(findViewById(android.R.id.content), msgResId, Snackbar.LENGTH_SHORT);
        snack.show();
    }

    private void saveSpot(ParkingSpot spot) {
        LocalStorageService.sendUpdateSpot(this, spot);
        HttpService.addSyncAction(this, new EditSpot(spot));
    }
}
