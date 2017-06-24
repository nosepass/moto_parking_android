package com.github.nosepass.motoparking.data

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import java.util.*

const val PARKING_SPOT_TABLE_NAME = "ParkingSpot"// can't really name this to anything but the default can I mr alpha room

@Entity(tableName = PARKING_SPOT_TABLE_NAME)
data class ParkingSpot (
//    @PrimaryKey(autoGenerate = true)
//    @Transient
//    val id: Long,
//    @SerializedName("id")
//    val uuid: String,
    @PrimaryKey
    val id: String,
    val name: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val paid: Boolean = false,
    val createdAt: Date,
    val updatedAt: Date
) : Parcelable {

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(description)
        dest.writeDouble(latitude)
        dest.writeDouble(longitude)
        dest.writeInt(if (paid) 1 else 0)
        dest.writeLong(createdAt.time)
        dest.writeLong(updatedAt.time)
    }

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<ParkingSpot> = object : Parcelable.Creator<ParkingSpot> {

            override fun createFromParcel(source: Parcel) = ParkingSpot(
                    id = source.readString(),
                    name = source.readString(),
                    description = source.readString(),
                    latitude = source.readDouble(),
                    longitude = source.readDouble(),
                    paid = source.readInt() == 1,
                    createdAt = Date(source.readLong()),
                    updatedAt = Date(source.readLong())
            )

            override fun newArray(size: Int): Array<ParkingSpot?> = arrayOfNulls(size)
        }
    }
}