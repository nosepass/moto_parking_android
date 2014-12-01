package com.github.nosepass.motoparking;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.github.nosepass.motoparking.db.ParcelableParkingSpot;
import com.github.nosepass.motoparking.db.ParkingSpot;
import com.github.nosepass.motoparking.views.MaterialProgressDrawable;

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
    public static final String EXTRA_HAS_PREVIEW = CLSNAME + ".EXTRA_HAS_PREVIEW";
    public static final String EXTRA_SHOW_DELETE = CLSNAME + ".EXTRA_SHOW_DELETE";
    /** the preview image is sent later, since it needs to be compressed for lameness purposes */
    public static final String SEND_PREVIEW_IMG = CLSNAME + ".SEND_PREVIEW_IMG";
    /** image byte data of where on the map the location is */
    public static final String EXTRA_PREVIEW_IMG = CLSNAME + ".EXTRA_PREVIEW_IMG";

    @InjectView(R.id.previewContainer)
    ViewGroup previewContainer;
    @InjectView(R.id.previewImageContainer)
    ViewGroup previewImageContainer;
    @InjectView(R.id.preview)
    ImageView preview;
    @InjectView(R.id.previewProgress)
    ContentLoadingProgressBar previewProgress;
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
    @InjectView(R.id.save)
    Button save;

    private BroadcastReceiver previewReceiver = new PreviewReceiver();
    private PreviewDecoderTask previewDecoder;
    private ParcelableParkingSpot spot;
    private byte[] imagePreviewData;
    private Bitmap decodedPreviewImage;

    public EditParkingSpotFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MyLog.v(TAG, "onCreate state=" + savedInstanceState);
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Retain the preview bitmap on rotate
    }

    @Override
    public void onAttach(Activity a) {
        MyLog.v(TAG, "onAttach");
        super.onAttach(a);
        getActivity().registerReceiver(previewReceiver, new IntentFilter(SEND_PREVIEW_IMG));
    }

    @Override
    public void onDetach() {
        MyLog.v(TAG, "onDetach");
        super.onDetach();
        getActivity().unregisterReceiver(previewReceiver);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        MyLog.v(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            imagePreviewData = savedInstanceState.getByteArray(EXTRA_PREVIEW_IMG);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        MyLog.v(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        if (imagePreviewData != null) {
            // save image data in case of low memory
            outState.putByteArray(EXTRA_PREVIEW_IMG, imagePreviewData);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MyLog.v(TAG, "onCreateView");

        spot = getArguments().getParcelable(EXTRA_SPOT);
        boolean hasPreview = getArguments().getBoolean(EXTRA_HAS_PREVIEW);
        boolean showDelete = getArguments().getBoolean(EXTRA_SHOW_DELETE);

        View rootView = inflater.inflate(R.layout.fragment_edit_parking_spot, container, false);
        ButterKnife.inject(this, rootView);

        Drawable d = getMaterialStyleLoaderIfNotLollipop(previewProgress.getContext(), previewProgress);
        if (d != null) {
            previewProgress.setIndeterminateDrawable(d);
        }

        if (hasPreview) {
            if (decodedPreviewImage != null) {
                MyLog.v(TAG, "loading cached preview image immediately");
                preview.setImageBitmap(decodedPreviewImage);
                previewProgress.setVisibility(View.GONE);
            } else {
                // Show loading spinner as the image gets transferred to this activity in
                // an annoyingly slow way (usually 1.5secs)
                previewImageContainer.setVisibility(View.INVISIBLE);
                if (imagePreviewData != null) {
                    // the fragment got recreated (low mem?), just re-decode bitmap
                    decodeImage(imagePreviewData);
                }
            }
        } else {
            // Edit does not show the preview image.
            previewContainer.setVisibility(View.GONE);
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

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClick();
            }
        });

        return rootView;
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

    private void decodeImage(byte[] imageData) {
        if (previewDecoder == null) {
            // Launch the asynctask only once, the fragment should eventually get
            // the proper result regardless of how many times onCreateView is called.
            MyLog.v(TAG, "launching decoder");
            previewDecoder = new PreviewDecoderTask();
            previewDecoder.execute(imageData);
        }
    }

    @SuppressWarnings("unchecked")
    private Drawable getMaterialStyleLoaderIfNotLollipop(Context c, View parent) {
        // Use a copy of the hidden support-v4 Material loader drawable
        // TODO use support-v4 via reflection instead of copying?
        if (MyUtil.beforeLollipop()) {
            MaterialProgressDrawable mpd = new MaterialProgressDrawable(c, parent);
            mpd.setBackgroundColor(0xFFFAFAFA);
            mpd.setAlpha(128);
            mpd.start();
            return mpd;
        }
        return null;
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
     * The image preview receiver. This fragment is launched immediately,
     * while a thread generating the preview data sends the image later.
     */
    private class PreviewReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            imagePreviewData = intent.getByteArrayExtra(EXTRA_PREVIEW_IMG);
            if (imagePreviewData.length < 20 * 1024) {
                // for some reason, probably my snapshot hack, an 8kb blank image can appear
                // log it
                MyLog.e(TAG, "image too small! - " + imagePreviewData.length);
            }
            if (!isDetached()) {
                MyLog.v(TAG, "got preview image data");
                decodeImage(imagePreviewData);
            } else {
                MyLog.e(TAG, "got preview message after fragment was destroyed??");
            }
        }
    }

    private class PreviewDecoderTask extends AsyncTask<byte[], Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(byte[]... params) {
            long start = System.currentTimeMillis();
            Bitmap b = null;
            try {
                byte[] imageData = params[0];
                b = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
            MyLog.v(TAG, "decoded image in %sms", System.currentTimeMillis() - start);
            return b;
        }

        @Override
        protected void onPostExecute(Bitmap b) {
            MyLog.v(TAG, "decode complete bitmap=" + b);
            decodedPreviewImage = b;
            if (!isDetached()) {
                if (b != null) {
                    preview.setImageBitmap(b);
                    previewImageContainer.setVisibility(View.VISIBLE);
                } else {
                    // an error happened. w/e.
                    previewImageContainer.setVisibility(View.INVISIBLE);
                }
                previewProgress.hide();
            } else {
                MyLog.v(TAG, "decoded preview after fragment was destroyed");
            }
        }
    }
}
