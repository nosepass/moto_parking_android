package com.github.nosepass.motoparking;

import android.os.Bundle;

import com.github.nosepass.motoparking.db.DaoSession;
import com.github.nosepass.motoparking.db.ParkingSpot;
import com.github.nosepass.motoparking.http.AddSpot;
import com.github.nosepass.motoparking.http.HttpService;
import com.github.nosepass.motoparking.http.ParkingDbDownload;

public class CreateSpotActivity extends BaseSpotActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment.getArguments().putBoolean(EditParkingSpotFragment.EXTRA_HAS_PREVIEW, true);
    }

    @Override
    public void onParkingSpotSaved(ParkingSpot spot) {
        finish();
        // save the new spot
        DaoSession s = ParkingDbDownload.daoMaster.newSession();
        s.getParkingSpotDao().insert(spot);
        HttpService.addSyncAction(this, new AddSpot(spot));
    }
}
