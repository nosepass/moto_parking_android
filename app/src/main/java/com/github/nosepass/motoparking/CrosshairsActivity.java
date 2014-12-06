package com.github.nosepass.motoparking;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.nosepass.motoparking.db.ParcelableParkingSpot;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * This lets you pick a spot on the map by moving the map behind an X.
 * It's used when you create a spot and when you move a spot.
 */
public class CrosshairsActivity extends BaseAppCompatActivity {
    private static final String TAG = "CrosshairsActivity";
    private static final String CLSNAME = CrosshairsActivity.class.getName();
    /** Center the map here, as the potential new location or the old location */
    public static final String EXTRA_MAP_CENTER = CLSNAME + ".EXTRA_SPOT";
    /** hacky flag to return lat/lng instead of calling create activity */
    public static final String EXTRA_RETURN_LOC = CLSNAME + ".EXTRA_RETURN_LOC";
    /** the result extra given to onActivityResult */
    public static final String EXTRA_SELECTED_LOCATION = CLSNAME + ".EXTRA_SELECTED_LOCATION";

    @InjectView(R.id.mapview)
    MapView mapView;
    @InjectView(R.id.complete)
    Button completeButton;

    private GoogleMap map;
    private boolean returnResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        returnResult = getIntent().getBooleanExtra(EXTRA_RETURN_LOC, false);

        setContentView(R.layout.activity_crosshairs);
        setSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.inject(this);

        mapView.onCreate(savedInstanceState);
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCompleteClick();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map == null) {
            map = mapView.getMap();
            LatLng latLong = getIntent().getParcelableExtra(EXTRA_MAP_CENTER);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, Constants.MOVE_PIN_ZOOM));
            map.setMyLocationEnabled(true);
        }
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
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

    private void onCompleteClick() {
        MyLog.v(TAG, "onCompleteClick");
        LatLng ll = map.getCameraPosition().target;
        if (returnResult) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_SELECTED_LOCATION, ll);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            openCreateActivity(ll);
        }
    }

    private void openCreateActivity(LatLng loc) {
        ParcelableParkingSpot newSpot = new ParcelableParkingSpot();
        newSpot.setLatitude(loc.latitude);
        newSpot.setLongitude(loc.longitude);
        Intent i = new Intent(CrosshairsActivity.this, CreateSpotActivity.class);
        i.putExtra(CreateSpotActivity.EXTRA_SPOT, newSpot);
        startActivity(i);
        // this is a hack to allow the new activity to come up and show it's progress spinner
        // before spending 200-500ms on taking the snapshot.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    try {
                        takeSnapshotAndSendToCreateSpot();
                        finish();
                    } catch (Exception e) {
                        MyLog.e(TAG, e);
                    }
                }
            }
        }, 50);
    }

    private void takeSnapshotAndSendToCreateSpot() {
        // capture a preview of the target's surrounding map
        final long start = System.currentTimeMillis();
        map.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(final Bitmap bitmap) {
                MyLog.v(TAG, "got snapshot in %sms", System.currentTimeMillis() - start);
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Intent i = new Intent(EditParkingSpotFragment.SEND_PREVIEW_IMG);
                            i.putExtra(EditParkingSpotFragment.EXTRA_PREVIEW_IMG, compressBitmap(bitmap));
                            sendBroadcast(i);
                        } catch (Exception e) {
                            MyLog.e(TAG, e);
                        }
                        bitmap.recycle();
                    }
                }.start();
            }
        });
    }

    // create a png byte array
    private byte[] compressBitmap(Bitmap b) {
        long start = System.currentTimeMillis();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] result = stream.toByteArray();
        MyLog.v(TAG, "compressed image with %skb in %sms", result.length / 1024, System.currentTimeMillis() - start);
        return result;
    }
}
