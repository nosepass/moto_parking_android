package com.github.nosepass.motoparking.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Parcelable is the most horrible api ever.
 * This writes byte presence flags before every value, to support null values by omitting them.
 */
public class ParcelableUtil {

    public static void writeBoolean(Parcel dest, Boolean value) {
        if (value == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeByte((byte) (value ? 0x01 : 0x00));
        }
    }

    public static Boolean readBoolean(Parcel in) {
        Boolean value = null;
        if (in.readByte() == 1) {
            value = in.readByte() != 0x00;
        }
        return value;
    }

    public static void writeString(Parcel dest, String value) {
        if (value == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeString(value);
        }
    }

    public static String readString(Parcel in) {
        String value = null;
        if (in.readByte() == 1) {
            value = in.readString();
        }
        return value;
    }

    public static void writeInteger(Parcel dest, Integer value) {
        if (value == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(value);
        }
    }

    public static Integer readInteger(Parcel in) {
        Integer value = null;
        if (in.readByte() == 1) {
            value = in.readInt();
        }
        return value;
    }

    public static void writeLong(Parcel dest, Long value) {
        if (value == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(value);
        }
    }

    public static Long readLong(Parcel in) {
        Long value = null;
        if (in.readByte() == 1) {
            value = in.readLong();
        }
        return value;
    }

    public static void writeDouble(Parcel dest, Double value) {
        if (value == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(value);
        }
    }

    public static Double readDouble(Parcel in) {
        Double value = null;
        if (in.readByte() == 1) {
            value = in.readDouble();
        }
        return value;
    }

    public static void writeDate(Parcel dest, Date value) {
        if (value == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(value.getTime());
        }
    }

    public static Date readDate(Parcel in) {
        Date value = null;
        if (in.readByte() == 1) {
            value = new Date(in.readLong());
        }
        return value;
    }

    public static void writeStringArray(Parcel dest, String[] value) {
        if (value == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeStringArray(value);
        }
    }

    public static String[] readStringArray(Parcel in) {
        String[] value = null;
        if (in.readByte() == 1) {
            value = in.createStringArray();
        }
        return value;
    }

    public static void writeList(Parcel dest, List<? extends Parcelable> value) {
        if (value == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeTypedList(value);
        }
    }

    public static <T> List<T> readList(Parcel in, Class<T> classObject, Parcelable.Creator<T> creator) {
        List<T> value = null;
        if (in.readByte() == 1) {
            value = new ArrayList<T>();
            in.readTypedList(value, creator);
        }
        return value;
    }
}