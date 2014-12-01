package com.github.nosepass.motoparking.db;


import android.os.Parcel;
import android.os.Parcelable;

import static com.github.nosepass.motoparking.util.ParcelableUtil.*;

/**
 * ugh, parcelable. why must you exist.
 */
public class ParcelableParkingSpot extends ParkingSpot implements Parcelable {

    public ParcelableParkingSpot() {
    }

    public ParcelableParkingSpot(ParkingSpot copyme) {
        setLocalId(copyme.getLocalId());
        setId(copyme.getId());
        setName(copyme.getName());
        setDescription(copyme.getDescription());
        setLatitude(copyme.getLatitude());
        setLongitude(copyme.getLongitude());
        setPaid(copyme.getPaid());
        setSpaces(copyme.getSpaces());
    }

    protected ParcelableParkingSpot(Parcel in) {
        setLocalId(readLong(in));
        setId(readLong(in));
        setName(readString(in));
        setDescription(readString(in));
        setLatitude(readDouble(in));
        setLongitude(readDouble(in));
        setPaid(readBoolean(in));
        setSpaces(readInteger(in));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeLong(dest, getLocalId());
        writeLong(dest, getId());
        writeString(dest, getName());
        writeString(dest, getDescription());
        writeDouble(dest, getLatitude());
        writeDouble(dest, getLongitude());
        writeBoolean(dest, getPaid());
        writeInteger(dest, getSpaces());
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ParcelableParkingSpot> CREATOR = new Parcelable.Creator<ParcelableParkingSpot>() {
        @Override
        public ParcelableParkingSpot createFromParcel(Parcel in) {
            return new ParcelableParkingSpot(in);
        }

        @Override
        public ParcelableParkingSpot[] newArray(int size) {
            return new ParcelableParkingSpot[size];
        }
    };
}
