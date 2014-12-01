package com.github.nosepass.motoparking;

import android.os.Bundle;

import com.github.nosepass.motoparking.db.DaoSession;
import com.github.nosepass.motoparking.db.ParkingSpot;
import com.github.nosepass.motoparking.http.DeleteSpot;
import com.github.nosepass.motoparking.http.EditSpot;
import com.github.nosepass.motoparking.http.HttpService;
import com.github.nosepass.motoparking.http.ParkingDbDownload;

public class EditSpotActivity extends BaseSpotActivity
        implements EditParkingSpotFragment.OnDeleteListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment.getArguments().putBoolean(EditParkingSpotFragment.EXTRA_SHOW_DELETE, true);
    }


    @Override
    public void onParkingSpotSaved(ParkingSpot spot) {
        finish();
        DaoSession s = ParkingDbDownload.daoMaster.newSession();
        s.getParkingSpotDao().update(spot);
        HttpService.addSyncAction(this, new EditSpot(spot));
    }

    @Override
    public void onParkingSpotDeleted(ParkingSpot spot) {
        finish();
        DaoSession s = ParkingDbDownload.daoMaster.newSession();
        s.getParkingSpotDao().delete(spot);
        HttpService.addSyncAction(this, new DeleteSpot(spot));
    }
}
