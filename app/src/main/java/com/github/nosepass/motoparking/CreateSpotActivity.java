package com.github.nosepass.motoparking;

import com.github.nosepass.motoparking.db.DaoSession;
import com.github.nosepass.motoparking.db.ParkingSpot;
import com.github.nosepass.motoparking.http.AddSpot;
import com.github.nosepass.motoparking.http.ParkingDbDownload;

public class CreateSpotActivity extends BaseSpotActivity {

    @Override
    public void onParkingSpotSaved(ParkingSpot spot) {
        finish();
        // save the new spot
        DaoSession s = ParkingDbDownload.daoMaster.newSession();
        s.getParkingSpotDao().insert(spot);
        MotoParkingApplication.addSyncAction(new AddSpot(prefs, spot));
    }
}
