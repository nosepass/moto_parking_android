package com.github.nosepass.motoparking;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.View;

import com.github.nosepass.motoparking.db.LocalStorageService;
import com.github.nosepass.motoparking.db.ParcelableParkingSpot;
import com.github.nosepass.motoparking.db.ParkingSpot;
import com.github.nosepass.motoparking.util.HashBiMap;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * This holds the various fragments and shows a nav bar to switch between them.
 */
public class MainActivity extends BaseAppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        GooglePlayGpsManager.AccurateLocationFoundCallback {
    private static final String TAG = "MainActivity";

    private GooglePlayGpsManager gps;
    private BroadcastReceiver parkingUpdateReceiver = new ParkingUpdateReceiver();
    private BroadcastReceiver spotUpdateReceiver = new SpotUpdateReceiver();

    private MapFragment mapFragment;
    @InjectView(R.id.floatingButton)
    FloatingActionButton addButton;

    private HashBiMap<Marker, ParkingSpot> markerToParkingSpot = new HashBiMap<>();
    private Location myLocation;
    private Set<ParkingSpot> spotsAdded = new HashSet<>();
    private List<ParkingSpot> spotsUpdated = new ArrayList<>();
    private Set<ParkingSpot> spotsDeleted = new HashSet<>();

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

        NavigationDrawerFragment navDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        navDrawerFragment.addDrawerItems(R.string.title_map_section, R.string.title_account_section, R.string.title_settings_section);
        navDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // this kinda redundant since the navdrawer calls it on inflate of the contentview
        createMapFragmentIfNeeded();

        layoutSpotMarkers();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFloatingAddClick();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MyLog.v(TAG, "onCreateOptionsMenu");
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        gps.start(50f, this);
        // TODO this can fail if the download completes while paused
        registerReceiver(parkingUpdateReceiver, new IntentFilter(LocalStorageService.SAVE_SPOTS_COMPLETE));

        // add any new markers
        updateSpotMarkers();
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
    public void onNavigationDrawerItemSelected(int position) {
        MyLog.v(TAG, "onNavigationDrawerItemSelected");
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                createMapFragmentIfNeeded();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, mapFragment)
                        .commit();
                // these all need null checks because setContentView calls onNavigationDrawerItemSelected
                if (addButton != null) {
                    addButton.show(true);
                }
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.title_activity_main);
                }
                break;
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new AccountFragment())
                        .commit();
                addButton.hide(true);
                getSupportActionBar().setTitle(R.string.title_account_section);
                break;
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new GeneralPreferenceFragment())
                        .commit();
                addButton.hide(true);
                getSupportActionBar().setTitle(R.string.title_settings_section);
                break;
        }
    }

    @Override
    public void onAccurateLocationFound(final Location l) {
        final boolean zoomPrefPresent = prefs.contains(PrefKeys.GPS_ZOOM);
        boolean zoomOnFix = prefs.getBoolean(PrefKeys.GPS_ZOOM, false) || !zoomPrefPresent;
        if (zoomOnFix) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    MyLog.v(TAG, "recentering map with new gps reading");
                    CameraUpdate newPos = CameraUpdateFactory.newLatLngZoom(
                            new LatLng(l.getLatitude(), l.getLongitude()), Constants.ON_GPS_FIX_ZOOM);
                    map.animateCamera(newPos);
                    if (!zoomPrefPresent) {
                        // by default, only do this one time at first run, and never again. It's a pretty spastic "feature"
                        prefs.edit().putBoolean(PrefKeys.GPS_ZOOM, false).apply();
                    }
                }
            });
        }
    }

    private void createMapFragmentIfNeeded() {
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance(getInitialMapOptions());
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    setUpMap(googleMap);
                }
            });
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

    private void setUpMap(final GoogleMap map) {
        map.setMyLocationEnabled(true);
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                // by default, the My Location button does not zoom >_>
                // so change it to zoom as well as center
                if (myLocation != null) {
                    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(
                            new LatLng(myLocation.getLatitude(), myLocation.getLongitude()),
                            Constants.ON_GPS_FIX_ZOOM);
                    map.animateCamera(cu);
                    return true;
                } else {
                    MyLog.v(TAG, "myLocation null!");
                    // I don't feel like handling the whole wait-for-fix-then-animate dealio
                    // so proceed default of centering only
                    return false;
                }
            }
        });
        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                myLocation = location;
            }
        });
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                MainActivity.this.onInfoWindowClick(marker);
            }
        });
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                MainActivity.this.onCameraChange(cameraPosition);
            }
        });
    }

    private void layoutSpotMarkers() {
        LocalStorageService.sendLoadSpots(getBaseContext(), new LocalStorageService.Callback<List<? extends ParkingSpot>>() {
            public void onSuccess(final List<? extends ParkingSpot> spots) {
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap map) {
                        layoutSpotMarkers(map, spots);
                    }
                });
            }

            public void onError() {
                MyLog.v(TAG, "unable to load spots");
            }
        });
    }

    private void layoutSpotMarkers(GoogleMap map, List<? extends ParkingSpot> spots) {
        MyLog.v(TAG, "laying out map markers");
        markerToParkingSpot.clear();
        map.clear();
        for (ParkingSpot spot : spots) {
            layoutOneMarker(map, spot);
        }
    }

    private void layoutOneMarker(GoogleMap map, ParkingSpot spot) {
        MyLog.v(TAG, "loading spot %s onto map", spot.getName());
        try {
            LatLng ll = new LatLng(spot.getLatitude(), spot.getLongitude());
            Marker m = map.addMarker(new MarkerOptions()
                            .position(ll)
                            .title(spot.getName())
                            .snippet(spot.getDescription())
            );
            markerToParkingSpot.put(m, spot);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    private void updateSpotMarkers() {
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                for (ParkingSpot newSpot : spotsAdded) {
                    layoutOneMarker(map, newSpot);
                }
                spotsAdded.clear();
                for (ParkingSpot spot : spotsUpdated) {
                    // TODO fix hashcode impl
                }
                for (ParkingSpot spot : spotsDeleted) {
                    // TODO ditto
                }
                // fallback to redrawing all markers since the above is not implemented
                if (spotsUpdated.size() > 0 || spotsDeleted.size() > 0) {
                    spotsUpdated.clear();
                    spotsDeleted.clear();
                    layoutSpotMarkers();
                }
            }
        });
    }


    private BitmapDescriptor createMarkerIcon(int count, boolean paid) {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(80, 80, conf);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint();
        p.setTextSize(35);
        p.setColor(Color.BLACK);

//        c.drawBitmap(BitmapFactory.decodeResource(getResources(),
//                R.drawable.user_picture_image), 0, 0, p);
        c.drawText(count + "", 30, 40, p);

        return null;
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

    private void onInfoWindowClick(Marker m) {
        MyLog.v(TAG, "onInfoWindowClick " + m);
        ParkingSpot s = markerToParkingSpot.get(m);
        if (s != null) {
            ParcelableParkingSpot spot = new ParcelableParkingSpot(s);
            Intent i = new Intent(MainActivity.this, EditSpotActivity.class);
            i.putExtra(EditSpotActivity.EXTRA_SPOT, spot);
            startActivity(i);
        } else {
            MyLog.e(TAG, "null marker!");
        }
    }

    private void onCameraChange(CameraPosition cameraPosition) {
        MyLog.v(TAG, "onCameraChange " + cameraPosition);
        // Save the camera position so it can be restored on next app launch
        String pos = String.format("%s,%s",
                cameraPosition.target.latitude, cameraPosition.target.longitude);
        prefs.edit()
                .putString(PrefKeys.CURRENT_POSITION, pos)
                .putFloat(PrefKeys.CURRENT_ZOOM, cameraPosition.zoom)
                .apply();
    }

    private class ParkingUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MyLog.v(TAG, "got data update");
            try {
                layoutSpotMarkers();
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
                spotsAdded.add(spot);
            } else if (LocalStorageService.UPDATE_SPOT_COMPLETE.equals(act)) {
                spotsUpdated.add(spot);
            } else if (LocalStorageService.DELETE_SPOT_COMPLETE.equals(act)) {
                spotsDeleted.add(spot);
            }
            if (activityActive) {
                updateSpotMarkers();
            }
        }
    }
}
