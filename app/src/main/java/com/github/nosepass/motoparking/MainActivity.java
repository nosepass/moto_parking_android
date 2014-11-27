package com.github.nosepass.motoparking;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.github.nosepass.motoparking.db.DaoSession;
import com.github.nosepass.motoparking.db.ParcelableParkingSpot;
import com.github.nosepass.motoparking.db.ParkingSpot;
import com.github.nosepass.motoparking.http.ParkingDbDownload;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;

/**
 * This holds the various fragments and shows a nav bar to switch between them.
 */
public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        GooglePlayGpsManager.AccurateLocationFoundCallback {
    private static final String TAG = "MainActivity";

    private SharedPreferences prefs;
    private GooglePlayGpsManager gps;

    private NavigationDrawerFragment navDrawerFragment;
    private MapFragment mapFragment;
    private GoogleMap map; // Might be null if Google Play services APK is not available.
    private View newTarget;
    private ImageButton addButton;
    private Button addCompleteButton;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence lastTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Debug.waitForDebugger();
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        gps = new GooglePlayGpsManager(this);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        navDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        lastTitle = getTitle();
        navDrawerFragment.addDrawerItems(R.string.title_map_section, R.string.title_placeholder_section);
        navDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        newTarget = findViewById(R.id.addTarget);

        addButton = (ImageButton) findViewById(R.id.floatingButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFloatingAddClick();
            }
        });

        addCompleteButton = (Button) findViewById(R.id.addComplete);
        addCompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddCompleteClick();
            }
        });

        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MyLog.v(TAG, "onCreateOptionsMenu");
        if (!navDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        gps.start(50f, this);

        // reset any modified add state
        clearAddWidgets();
        // helps with reset and also adds any new markers
        layoutSpotMarkers();
    }

    @Override
    public void onPause() {
        super.onPause();
        gps.stop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        MyLog.v(TAG, "onNavigationDrawerItemSelected");
        // the map fragment needs to exist before we try to use it
        // (this can be called by setContentView)
        setUpMapIfNeeded();
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                MyLog.v(TAG, "mapFragment " + mapFragment);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, mapFragment)
                        .commit();
                break;
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
                newTarget.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onAccurateLocationFound(Location l) {
        if (map != null) {
            MyLog.v(TAG, "recentering map with new gps reading");
            int zoom = prefs.getInt(PrefKeys.GPS_FIX_ZOOM, 14);
            CameraUpdate newPos = CameraUpdateFactory.newLatLngZoom(
                    new LatLng(l.getLatitude(), l.getLongitude()), zoom);
            map.animateCamera(newPos);
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.
     * <p>
     * If it isn't installed {@link com.google.android.gms.maps.MapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this Activity after following the prompt and correctly
     * installing/updating/enabling the Google Play services.
     */
    private void setUpMapIfNeeded() {
        if (mapFragment == null) {
            LatLng latLong = getInitialLatLng();
            int zoom = prefs.getInt(PrefKeys.STARTING_ZOOM, 12);

            MyLog.v(TAG, "centering map on %s with zoom=%s", latLong, zoom);
            GoogleMapOptions options = new GoogleMapOptions().camera(
                    CameraPosition.fromLatLngZoom(latLong, zoom));
            mapFragment = MapFragment.newInstance(options);
        }
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the MapFragment.
            map = mapFragment.getMap();
            if (map != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #map} is not null.
     */
    private void setUpMap() {
        final LatLng initial = getInitialLatLng();
        map.setMyLocationEnabled(true);
        ParkingDbDownload.initDb(getBaseContext()); // TODO this should be somewhere more sane
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                MyLog.v(TAG, "downloading...");
                try {
                    ParkingDbDownload dl = new ParkingDbDownload(prefs, initial.latitude, initial.longitude);
                    dl.executeHttpRequest();
                } catch (Exception e) {
                    MyLog.e(TAG, e);
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                if (!isFinishing()) {
                    layoutSpotMarkers();
                }
            }
        }.execute();
    }

    private void layoutSpotMarkers() {
        DaoSession s = ParkingDbDownload.daoMaster.newSession();
        for (ParkingSpot spot : s.getParkingSpotDao().loadAll()) {
            MyLog.v(TAG, "loading spot %s onto map", spot.getName());
            try {
                LatLng ll = new LatLng(spot.getLatitude(), spot.getLongitude());
                map.addMarker(new MarkerOptions()
                                .position(ll)
                                .title(spot.getName())
                                .snippet(spot.getDescription())
                );
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        }
    }

    private void clearMarkers() {
        map.clear();
    }

    private LatLng getInitialLatLng() {
        return MyUtil.getInitialLatLng(prefs);
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

    private void onSectionAttached(int number) {
        switch (number) {
            case 1:
                lastTitle = getString(R.string.title_map_section);
                break;
            case 2:
                lastTitle = getString(R.string.title_placeholder_section);
                break;
        }
    }

    private void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        //todo
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setTitle(lastTitle);
    }

    private void clearAddWidgets() {
        //boolean wasInAddMode = newTarget.getVisibility() == View.VISIBLE;
        newTarget.setVisibility(View.GONE);
        addButton.setVisibility(View.VISIBLE);
        addCompleteButton.setVisibility(View.GONE);
//        if (wasInAddMode) {
//            layoutSpotMarkers();
//        }
    }

    private void onFloatingAddClick() {
        MyLog.v(TAG, "onFloatingAddClick");
        addButton.setVisibility(View.GONE);
        clearMarkers();
        newTarget.setVisibility(View.VISIBLE);
        addCompleteButton.setVisibility(View.VISIBLE);
    }

    private void onAddCompleteClick() {
        MyLog.v(TAG, "onAddCompleteClick");
        if (map == null) return;
        clearAddWidgets();
        LatLng ll = map.getCameraPosition().target;
        ParcelableParkingSpot newSpot = new ParcelableParkingSpot();
        newSpot.setLatitude(ll.latitude);
        newSpot.setLongitude(ll.longitude);
        Intent i = new Intent(MainActivity.this, CreateSpotActivity.class);
        i.putExtra(EditParkingSpotFragment.EXTRA_SPOT, newSpot);
        i.putExtra(EditParkingSpotFragment.EXTRA_HAS_PREVIEW, true);
        startActivity(i);
        // this is a hack to allow the new activity to come up and show it's progress spinner
        // before spending 200-500ms on taking the snapshot.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    try {
                        takeSnapshotAndSendToCreateSpot();
                    } catch (Exception e) {
                        MyLog.e(TAG, e);
                    }
                }
            }
        }, 100);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_nav_placeholder, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
