package com.github.nosepass.motoparking;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
        map.addMarker(new MarkerOptions().position(getInitialLatLng()).title("Marker"));
        map.setMyLocationEnabled(true);
    }

    private LatLng getInitialLatLng() {
        return MyUtil.getInitialLatLng(prefs);
    }

    /**
     * @param tag id to tag marker with to look up details for it in the db
     * @param count number of spots to display as a digit on the marker
     * @param paid red or green marker, red if paid
     */
    private MarkerOptions createMarker(LatLng loc, String tag, String title, int count, boolean paid) {
        return new MarkerOptions()
                .position(loc)
                .flat(true)
                .title("Marker");
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
