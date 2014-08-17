package com.github.nosepass.motoparking;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * activity to stuff Google Play Services error dialogs into
 */
public class GmsErrorActivity extends Activity {
    private static final String TAG = "GmsErrorActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        int error = getIntent().getExtras().getInt("error");
        Dialog d = GooglePlayServicesUtil.getErrorDialog(error, this, 0);
        if (d != null) {
            d.show();
        } else {
            // error = SERVICE_INVALID?
            // not sure what to do
            MyLog.e(TAG, "no error dialog provided");
            finish();
        }
    }
}
