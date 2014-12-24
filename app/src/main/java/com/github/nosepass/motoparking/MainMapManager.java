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
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages the markers on a map. They have colors and sizes at certain zoom levels n shit.
 */
public class MainMapManager implements GooglePlayGpsManager.AccurateLocationFoundCallback {
    private static final String TAG = "MainMapManager";
    private static final MarkerOptions MARKER_OPTIONS = new MarkerOptions();

    private Activity activity;
    private Context context;
    private SharedPreferences prefs;

    private MapFragment mapFragment;

    private HashBiMap<Marker, ParkingSpot> markerToParkingSpot = new HashBiMap<>();
    private Location myLocation;
    Set<ParkingSpot> spotsAdded = new HashSet<>();
    List<ParkingSpot> spotsUpdated = new ArrayList<>();
    Set<ParkingSpot> spotsDeleted = new HashSet<>();
    private BitmapDescriptor mapPinIcon;
    private BitmapDescriptor measleIconRed;
    private BitmapDescriptor measleIconBlue;


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
        mapPinIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        measleIconRed = BitmapDescriptorFactory.fromResource(R.drawable.measle_red);
        measleIconBlue = BitmapDescriptorFactory.fromResource(R.drawable.measle_blue);
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
        long start = System.currentTimeMillis();
        markerToParkingSpot.clear();

        map.clear();
        boolean useMeasles = map.getCameraPosition().zoom <= Constants.MEASLE_ZOOM;
        for (ParkingSpot spot : spots) {
            layoutOneMarker(map, spot, useMeasles);
        }
        MyLog.v(TAG, "%s markers laid out in in %sms", spots.size(), System.currentTimeMillis() - start);
    }

    private void layoutOneMarker(GoogleMap map, ParkingSpot spot, boolean measleInsteadOfPin) {
        MyLog.v(TAG, "loading spot %s onto map", spot.getName());
        try {
            LatLng ll = new LatLng(spot.getLatitude(), spot.getLongitude());
            Marker m = map.addMarker(MARKER_OPTIONS
                    .position(ll)
                    .title(spot.getName())
                    .snippet(spot.getDescription())
                    .alpha(0.8f) // ghetto way to spot double markers
                    .icon(getMarkerIcon(measleInsteadOfPin, spot))
            );
            markerToParkingSpot.put(m, spot);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    private BitmapDescriptor getMarkerIcon(boolean measleInsteadOfPin, ParkingSpot spot) {
        if (measleInsteadOfPin) {
            return spot.getPaid() ? measleIconRed : measleIconBlue;
        } else {
            return mapPinIcon;
        }
    }

    private void updateMeaslesOrPins(float zoom) {
        boolean useMeasles = zoom <= Constants.MEASLE_ZOOM;
        for (Map.Entry<Marker, ParkingSpot> e : markerToParkingSpot.entrySet()) {
            Marker m = e.getKey();
            ParkingSpot spot = e.getValue();
            m.setIcon(getMarkerIcon(useMeasles, spot));
        }
    }

    public void updateSpotMarkers() {
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                boolean useMeasles = map.getCameraPosition().zoom <= Constants.MEASLE_ZOOM;
                for (ParkingSpot newSpot : spotsAdded) {
                    layoutOneMarker(map, newSpot, useMeasles);
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
        float oldZoom = prefs.getFloat(PrefKeys.CURRENT_ZOOM, Constants.MEASLE_ZOOM + 1);
        prefs.edit()
                .putString(PrefKeys.CURRENT_POSITION, pos)
                .putFloat(PrefKeys.CURRENT_ZOOM, cameraPosition.zoom)
                .apply();
        // Decide whether to show pins or measles depending on zoom level
        boolean wasMeasleZoom = oldZoom <= Constants.MEASLE_ZOOM;
        boolean wasPinZoom = oldZoom > Constants.MEASLE_ZOOM;
        boolean isMeasleZoom = cameraPosition.zoom <= Constants.MEASLE_ZOOM;
        boolean changeNeeded = wasMeasleZoom != isMeasleZoom || wasPinZoom == isMeasleZoom;
        if (changeNeeded) {
            MyLog.v(TAG, "toggling pins vs measles at zoom %s, old %s", cameraPosition.zoom, oldZoom);
            updateMeaslesOrPins(cameraPosition.zoom);
        }
    }
}
