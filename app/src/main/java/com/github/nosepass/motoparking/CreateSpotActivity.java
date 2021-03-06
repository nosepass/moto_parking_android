package com.github.nosepass.motoparking;

import com.github.nosepass.motoparking.db.LocalStorageService;
import com.github.nosepass.motoparking.db.ParcelableParkingSpot;
import com.github.nosepass.motoparking.db.ParkingSpot;
import com.github.nosepass.motoparking.http.AddSpot;
import com.github.nosepass.motoparking.http.HttpService;

public class CreateSpotActivity extends BaseSpotActivity {

    @Override
    public void onParkingSpotSaved(ParkingSpot spot) {
        finish();
        LocalStorageService.populateUuid(spot);
        LocalStorageService.sendInsertSpot(this, new ParcelableParkingSpot(spot), new LocalStorageService.Callback<ParcelableParkingSpot>());
        HttpService.addSyncAction(getBaseContext(), new AddSpot(spot));
    }
}
