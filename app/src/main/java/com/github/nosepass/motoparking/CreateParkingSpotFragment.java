package com.github.nosepass.motoparking;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.github.nosepass.motoparking.db.DaoSession;
import com.github.nosepass.motoparking.db.ParkingSpot;
import com.github.nosepass.motoparking.http.AddSpot;
import com.github.nosepass.motoparking.http.ParkingDbDownload;
import com.google.android.gms.maps.model.LatLng;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * This has all the form fields for creating a new parking spot.
 * It saves the data and notifies the container activity when done.
 */
public class CreateParkingSpotFragment extends Fragment {
    private static final String TAG = "CreateParkingSpotFragment";
    private static final String CLSNAME = CreateParkingSpotFragment.class.getName();
    public static final String EXTRA_PREVIEW_FILE = CLSNAME + ".EXTRA_PREVIEW_FILE";
    public static final String EXTRA_SPOT_LATLNG = CLSNAME + ".EXTRA_SPOT_LATLNG";

    private SharedPreferences prefs;

    @InjectView(R.id.preview)
    ImageView preview;
    @InjectView(R.id.name)
    EditText name;
    @InjectView(R.id.desc)
    EditText desc;
    @InjectView(R.id.count)
    EditText count;
    @InjectView(R.id.paid)
    CheckBox paid;
    @InjectView(R.id.save)
    Button save;

    private String previewFile;
    private LatLng newSpot;

    public CreateParkingSpotFragment() {
    }

    @Override
    public void onAttach(Activity a) {
        MyLog.v(TAG, "onAttach");
        super.onAttach(a);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        MyLog.v(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MyLog.v(TAG, "onCreateView");

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        previewFile = getArguments().getString(EXTRA_PREVIEW_FILE);
        newSpot = getArguments().getParcelable(EXTRA_SPOT_LATLNG);

        View rootView = inflater.inflate(R.layout.fragment_create_parking_spot, container, false);
        ButterKnife.inject(this, rootView);

        loadPreviewImage(getActivity(), preview);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClick();
            }
        });

        return rootView;
    }

    private void loadPreviewImage(Context c, ImageView preview) {
        try {
            Bitmap b = BitmapFactory.decodeFile(previewFile);
            preview.setImageBitmap(b);
        } catch (Exception e) {
            MyLog.e(TAG, e);
            preview.setVisibility(View.INVISIBLE);
        }
    }

    private void onSaveClick() {
        MyLog.v(TAG, "onSaveClick");

        ParkingSpot spot = new ParkingSpot();
        spot.setName(name.getText() + "");
        spot.setDescription(desc.getText() + "");
        try {
            spot.setSpaces(Integer.parseInt(count.getText() + ""));
        } catch (NumberFormatException e) {
            MyLog.e(TAG, e);
        }
        spot.setPaid(paid.isChecked());
        spot.setLatitude(newSpot.latitude);
        spot.setLongitude(newSpot.longitude);

        DaoSession s = ParkingDbDownload.daoMaster.newSession();
        s.getParkingSpotDao().insert(spot);
        MotoParkingApplication.addSyncAction(new AddSpot(prefs, spot));

        if (getActivity() instanceof OnSaveListener) {
            ((OnSaveListener)getActivity()).onParkingSpotSaved();
        } else {
            MyLog.e(TAG, "unable to signal save completion!");
        }
    }

    /**
     * An activity can override this to be notified when save is clicked.
     */
    interface OnSaveListener {
        public void onParkingSpotSaved();
    }
}
