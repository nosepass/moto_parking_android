package com.github.nosepass.motoparking;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.github.nosepass.motoparking.db.ParcelableParkingSpot;
import com.github.nosepass.motoparking.db.ParkingSpot;
import com.github.nosepass.motoparking.db.ParkingSpotDao;
import com.github.nosepass.motoparking.http.ParkingDbDownload;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * This has all the form fields for a parking spot.
 * It saves the data and notifies the container activity when done.
 */
public class EditParkingSpotFragment extends Fragment {
    private static final String TAG = "EditParkingSpotFragment";
    private static final String CLSNAME = EditParkingSpotFragment.class.getName();
    /** A spot for editing, or a new ParkingSpot object with only the lat/lng populated */
    public static final String EXTRA_SPOT = CLSNAME + ".EXTRA_SPOT";
    public static final String EXTRA_SHOW_EDIT_CONTROLS = CLSNAME + ".EXTRA_SHOW_EDIT_CONTROLS";

    @InjectView(R.id.preview)
    MapView previewMap;
    @InjectView(R.id.name)
    EditText name;
    @InjectView(R.id.desc)
    EditText desc;
    @InjectView(R.id.count)
    EditText count;
    @InjectView(R.id.paid)
    CheckBox paid;
    @InjectView(R.id.delete)
    Button delete;
    @InjectView(R.id.move)
    Button move;
    @InjectView(R.id.save)
    Button save;

    private ParcelableParkingSpot spot;

    public EditParkingSpotFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MyLog.v(TAG, "onCreate state=" + savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MyLog.v(TAG, "onCreateView");

        spot = getArguments().getParcelable(EXTRA_SPOT);
        boolean showDelete, showMove;
        showDelete = showMove = getArguments().getBoolean(EXTRA_SHOW_EDIT_CONTROLS);

        View rootView = inflater.inflate(R.layout.fragment_edit_parking_spot, container, false);
        ButterKnife.inject(this, rootView);

        previewMap.onCreate(savedInstanceState);
        previewMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                // geeze Google makes it hard to use MapView instead of MapFragment
                // this is here because sometimes CameraUpdateFactory throws an NPE if you resume the activity later
                // for added wtf, it only happens when you switch to Maps.apk then back to this app
                // why the f does a static method have state to it
                // why is the map not actually ready in onMapReady?
                MapsInitializer.initialize(getActivity());

                map.getUiSettings().setMapToolbarEnabled(false);
                LatLng latLong = new LatLng(spot.getLatitude(), spot.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, Constants.MOVE_PIN_ZOOM));
            }
        });

        ParkingSpotDao dao = ParkingDbDownload.daoMaster.newSession().getParkingSpotDao();
        if (spot.getLocalId() != null) {
            // need to reload spot to get asynchronusly assigned server id (mb I should've used UUIDs)
            dao.refresh(spot); // TODO move out of main thread, preferably have MainActivity or a service do this
        }
        name.setText(spot.getName());
        desc.setText(spot.getDescription());
        count.setText(spot.getSpaces() == null ? "" : spot.getSpaces() + "");
        paid.setChecked(spot.getPaid() == null ? false : spot.getPaid());

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteClick();
            }
        });
        if (!showDelete) {
            delete.setVisibility(View.GONE);
        }

        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMoveClick();
            }
        });
        if (!showMove) {
            move.setVisibility(View.GONE);
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClick();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (previewMap != null) previewMap.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (previewMap != null) previewMap.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (previewMap != null) previewMap.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (previewMap != null) previewMap.onLowMemory();
    }

    private void onDeleteClick() {
        MyLog.v(TAG, "onDeleteClick");

        new AlertDialog.Builder(getActivity())
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setMessage(R.string.edit_spot_delete_confirm)
                .setPositiveButton(R.string.edit_spot_delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        if (getActivity() instanceof OnDeleteListener) {
                            ((OnDeleteListener)getActivity()).onParkingSpotDeleted(spot);
                        } else {
                            MyLog.e(TAG, "unable to signal delete!");
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void onMoveClick() {
        MyLog.v(TAG, "onMoveClick");
        if (getActivity() instanceof OnMoveListener) {
            ((OnMoveListener)getActivity()).onParkingSpotMove(spot);
        } else {
            MyLog.e(TAG, "unable to signal move!");
        }
    }

    private void onSaveClick() {
        MyLog.v(TAG, "onSaveClick");

        name.setError(null);
        count.setError(null);

        String reqmsg = getString(R.string.error_field_required);
        boolean hasError = false;
        if (TextUtils.isEmpty(name.getText())) {
            name.setError(reqmsg);
            hasError = true;
        }
        if (TextUtils.isEmpty(count.getText())) {
            count.setError(reqmsg);
            hasError = true;
        }
        if (hasError) {
            return;
        }

        spot.setName(name.getText() + "");
        spot.setDescription(desc.getText() + "");
        try {
            spot.setSpaces(Integer.parseInt(count.getText() + ""));
        } catch (NumberFormatException e) {
            MyLog.e(TAG, e);
        }
        spot.setPaid(paid.isChecked());

        if (getActivity() instanceof OnSaveListener) {
            ((OnSaveListener)getActivity()).onParkingSpotSaved(spot);
        } else {
            MyLog.e(TAG, "unable to signal save completion!");
        }
    }

    /**
     * An activity can override this to be notified when save is clicked.
     */
    interface OnSaveListener {
        public void onParkingSpotSaved(ParkingSpot spot);
    }

    /**
     * An activity can override this to be notified when delete is clicked.
     */
    interface OnDeleteListener {
        public void onParkingSpotDeleted(ParkingSpot spot);
    }

    /**
     * An activity can override this to be notified when move is clicked.
     */
    interface OnMoveListener {
        public void onParkingSpotMove(ParkingSpot spot);
    }
}
