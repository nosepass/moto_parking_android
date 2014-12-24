package com.github.nosepass.motoparking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages the markers on a map. They have colors and sizes at certain zoom levels n shit.
 */
public class MainMapManager implements GooglePlayGpsManager.AccurateLocationFoundCallback {
    private static final String TAG = "MainMapManager";

    private Activity activity;
    private Context context;
    private SharedPreferences prefs;

    private MapFragment mapFragment;

    private HashBiMap<Marker, ParkingSpot> markerToParkingSpot = new HashBiMap<>();
    private Location myLocation;
    Set<ParkingSpot> spotsAdded = new HashSet<>();
    List<ParkingSpot> spotsUpdated = new ArrayList<>();
    Set<ParkingSpot> spotsDeleted = new HashSet<>();


    public MainMapManager(Activity parent, MapFragment mapFragment) {
        this.activity = parent;
        this.context = activity;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.mapFragment = mapFragment;
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                setUpMap(googleMap);
            }
        });
    }

    public int markerCount() {
        return markerToParkingSpot.size();
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
                MainMapManager.this.onInfoWindowClick(marker);
            }
        });
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                MainMapManager.this.onCameraChange(cameraPosition);
            }
        });
    }

    public void layoutSpotMarkers() {
        LocalStorageService.sendLoadSpots(context, new LocalStorageService.Callback<List<? extends ParkingSpot>>() {
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
            Marker m = map.addMarker(createMarker(spot));
            markerToParkingSpot.put(m, spot);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    public void updateSpotMarkers() {
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

    private MarkerOptions createMarker(ParkingSpot spot) {
        LatLng ll = new LatLng(spot.getLatitude(), spot.getLongitude());
        MarkerOptions m = new MarkerOptions()
                .position(ll)
                .title(spot.getName())
                .snippet(spot.getDescription())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .alpha(0.8f) // ghetto way to spot double markers
                ;
        if (!spot.getPaid()) {
            // I'm going with ghosty instead of green for unpaid since I might use green for
            // available spots
            m.alpha(0.5f);
            //m.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }
        return m;
    }

    private MarkerOptions createMeasle(ParkingSpot spot) {
        LatLng ll = new LatLng(spot.getLatitude(), spot.getLongitude());
        MarkerOptions m = new MarkerOptions()
                .position(ll)
                .title(spot.getName())
                .snippet(spot.getDescription())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .alpha(0.8f) // ghetto way to spot double markers
                ;
        if (!spot.getPaid()) {
            // I'm going with ghosty instead of green for unpaid since I might use green for
            // available spots
            m.alpha(0.5f);
            //m.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }
        return m;
    }

    private void onInfoWindowClick(Marker m) {
        MyLog.v(TAG, "onInfoWindowClick " + m);
        ParkingSpot s = markerToParkingSpot.get(m);
        if (s != null) {
            ParcelableParkingSpot spot = new ParcelableParkingSpot(s);
            Intent i = new Intent(context, EditSpotActivity.class);
            i.putExtra(EditSpotActivity.EXTRA_SPOT, spot);
            activity.startActivity(i);
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
}