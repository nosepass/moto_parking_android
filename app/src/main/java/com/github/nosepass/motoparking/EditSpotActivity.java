package com.github.nosepass.motoparking;

import com.github.nosepass.motoparking.db.DaoSession;
import com.github.nosepass.motoparking.db.ParkingSpot;
import com.github.nosepass.motoparking.http.ParkingDbDownload;

public class EditSpotActivity extends BaseSpotActivity {

    @Override
    public void onParkingSpotSaved(ParkingSpot spot) {
        finish();
        DaoSession s = ParkingDbDownload.daoMaster.newSession();
        s.getParkingSpotDao().update(spot);
        throw new RuntimeException("edit not implemented!");
        //MotoParkingApplication.addSyncAction(new AddSpot(prefs, spot));
    }
}
