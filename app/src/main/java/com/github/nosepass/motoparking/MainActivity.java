package com.github.nosepass.motoparking;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.View;

import com.github.nosepass.motoparking.db.LocalStorageService;
import com.github.nosepass.motoparking.db.ParcelableParkingSpot;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.melnykov.fab.FloatingActionButton;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * This holds the various fragments and shows a nav bar to switch between them.
 */
public class MainActivity extends BaseAppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    private static final String TAG = "MainActivity";

    private GooglePlayGpsManager gps;
    private MainMapManager mapManager;
    private BroadcastReceiver parkingUpdateReceiver = new ParkingUpdateReceiver();
    private BroadcastReceiver spotUpdateReceiver = new SpotUpdateReceiver();

    private NavigationDrawerFragment navDrawerFragment;
    private MapFragment mapFragment;
    @InjectView(R.id.floatingButton)
    FloatingActionButton addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Debug.waitForDebugger();
        super.onCreate(savedInstanceState);
        gps = new GooglePlayGpsManager(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(LocalStorageService.INSERT_SPOT_COMPLETE);
        filter.addAction(LocalStorageService.UPDATE_SPOT_COMPLETE);
        filter.addAction(LocalStorageService.DELETE_SPOT_COMPLETE);
        registerReceiver(spotUpdateReceiver, filter);

        setContentView(R.layout.activity_main);
        setSupportActionBar();
        ButterKnife.inject(this);

        navDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        navDrawerFragment.addDrawerItems(R.string.title_map_section, R.string.title_account_section, R.string.title_settings_section);
        navDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        String mapTag = "map";
        if ((mapFragment = (MapFragment) getFragmentManager().findFragmentByTag(mapTag)) != null) {
            MyLog.v(TAG, "found retained mapfragment instance");
            mapManager = new MainMapManager(this, mapFragment);
        } else {
            mapFragment = MapFragment.newInstance(getInitialMapOptions());
            mapFragment.setRetainInstance(true); // speed up screen rotation
            mapManager = new MainMapManager(this, mapFragment);
            getFragmentManager().beginTransaction()
                    .add(R.id.mapContainer, mapFragment, mapTag)
                    .commit();
            mapManager.layoutSpotMarkers();
        }

        addButton.hide(false);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFloatingAddClick();
            }
        });
        AnimationDrawable currentAnim = (AnimationDrawable) addButton.getDrawable();
        currentAnim.stop(); // initial setVisibilty by the button triggers the percent animation, stop it
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MyLog.v(TAG, "onCreateOptionsMenu");
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        gps.start(50f, mapManager);
        registerReceiver(parkingUpdateReceiver, new IntentFilter(LocalStorageService.SAVE_SPOTS_COMPLETE));

        if (mapManager.markerCount() == 0) {
            // in case download or sql load completed while activity was in background, retry db load
            mapManager.layoutSpotMarkers();
        } else {
            // add any new markers
            mapManager.updateSpotMarkers();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        gps.stop();
        unregisterReceiver(parkingUpdateReceiver);
    }

    @Override
    public void onLowMemory() {
        MyLog.v(TAG, "onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(spotUpdateReceiver);
    }

    @Override
    public void onBackPressed() {
        // If in another fragment, pop back to main. Otherwise exit activity.
        if (getFragmentManager().popBackStackImmediate()) {
            // update the drawer and the titlebar as well, and also save section state
            navDrawerFragment.selectItem(0);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        MyLog.v(TAG, "onNavigationDrawerItemSelected " + position);
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                fragmentManager.popBackStack();
                fragmentManager.beginTransaction()
                        .show(mapFragment)
                        .commit();
                addButton.show(true);
                getSupportActionBar().setTitle(R.string.title_activity_main);
                break;
            case 1:
                fragmentManager.popBackStack();
                fragmentManager.beginTransaction()
                        .hide(mapFragment)
                        .add(R.id.container, new AccountFragment())
                        .addToBackStack(null)
                        .commit();
                addButton.hide(true);
                getSupportActionBar().setTitle(R.string.title_account_section);
                break;
            case 2:
                fragmentManager.popBackStack();
                fragmentManager.beginTransaction()
                        .hide(mapFragment)
                        .add(R.id.container, new GeneralPreferenceFragment())
                        .addToBackStack(null)
                        .commit();
                addButton.hide(true);
                getSupportActionBar().setTitle(R.string.title_settings_section);
                break;
        }
    }

    private GoogleMapOptions getInitialMapOptions() {
        LatLng ll;
        float zoom;
        if (prefs.contains(PrefKeys.CURRENT_POSITION) && prefs.contains(PrefKeys.CURRENT_ZOOM)) {
            MyLog.v(TAG, "found saved position");
            ll = MyUtil.getPrefLatLng(prefs, PrefKeys.CURRENT_POSITION);
            zoom = prefs.getFloat(PrefKeys.CURRENT_ZOOM, 12);
        } else {
            MyLog.v(TAG, "no saved position, starting at default");
            ll = MyUtil.getPrefLatLng(prefs, PrefKeys.STARTING_LAT_LONG);
            zoom = prefs.getInt(PrefKeys.STARTING_ZOOM, 12);
        }

        MyLog.v(TAG, "centering map on %s with zoom=%s", ll, zoom);
        return new GoogleMapOptions().camera(CameraPosition.fromLatLngZoom(ll, zoom));
    }

    private void onFloatingAddClick() {
        MyLog.v(TAG, "onFloatingAddClick");
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                // TODO maybe just call CreateSpotActivity and somehow make it handle the switch to CrosshairsActivity in a fast way
                Intent i = new Intent(MainActivity.this, CrosshairsActivity.class);
                i.putExtra(CrosshairsActivity.EXTRA_MAP_CENTER, map.getCameraPosition().target);
                i.putExtra(CrosshairsActivity.EXTRA_TITLE, getString(R.string.title_activity_crosshairs_add));
                startActivity(i);
            }
        });
    }

    private class ParkingUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MyLog.v(TAG, "got data update");
            try {
                mapManager.layoutSpotMarkers();
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        }
    }

    /**
     * Receive updates to individual spots
     */
    private class SpotUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String act = intent.getAction();
            String responseType = act == null ? "" : act.replace(LocalStorageService.class.getName(), "");
            MyLog.v(TAG, "got spot update " + responseType);
            ParcelableParkingSpot spot = intent.getParcelableExtra(LocalStorageService.EXTRA_SPOT);
            if (LocalStorageService.INSERT_SPOT_COMPLETE.equals(act)) {
                mapManager.spotsAdded.add(spot);
            } else if (LocalStorageService.UPDATE_SPOT_COMPLETE.equals(act)) {
                mapManager.spotsUpdated.add(spot);
            } else if (LocalStorageService.DELETE_SPOT_COMPLETE.equals(act)) {
                mapManager.spotsDeleted.add(spot);
            }
            if (activityActive) {
                mapManager.updateSpotMarkers();
            }
        }
    }
}
