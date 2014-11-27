package com.github.nosepass.motoparking;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.github.nosepass.motoparking.db.ParcelableParkingSpot;
import com.github.nosepass.motoparking.db.ParkingSpot;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * This has all the form fields for a parking spot.
 * It saves the data and notifies the container activity when done.
 */
public class EditParkingSpotFragment extends Fragment {
    private static final String TAG = "CreateParkingSpotFragment";
    private static final String CLSNAME = EditParkingSpotFragment.class.getName();
    /** A spot for editing, or a new ParkingSpot object with only the lat/lng populated */
    public static final String EXTRA_SPOT = CLSNAME + ".EXTRA_SPOT";
    /** image byte data of where on the map the location is */
    public static final String EXTRA_PREVIEW_IMG = CLSNAME + ".EXTRA_PREVIEW_IMG";

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

    private ParcelableParkingSpot spot;

    public EditParkingSpotFragment() {
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

        spot = getArguments().getParcelable(EXTRA_SPOT);
        byte[] compressedPreviewImage = getArguments().getByteArray(EXTRA_PREVIEW_IMG);

        View rootView = inflater.inflate(R.layout.fragment_edit_parking_spot, container, false);
        ButterKnife.inject(this, rootView);

        loadPreviewImage(preview, compressedPreviewImage);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClick();
            }
        });

        return rootView;
    }

    private void loadPreviewImage(ImageView preview, byte[] imageData) {
        if (imageData != null) {
            try {
                Bitmap b = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                preview.setImageBitmap(b);
            } catch (Exception e) {
                MyLog.e(TAG, e);
                preview.setVisibility(View.INVISIBLE);
            }
        } else {
            // Edit does not show the preview image.
            preview.setVisibility(View.GONE);
        }
    }

    private void onSaveClick() {
        MyLog.v(TAG, "onSaveClick");

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
}
