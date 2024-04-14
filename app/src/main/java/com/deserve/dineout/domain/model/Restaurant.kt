package com.deserve.dineout.domain.model

import android.os.Parcel
import android.os.Parcelable

data class Restaurant(
    val name: String,
    val rating: Double,
    val vicinity: String,
    val icon: String,
    val geometry: Geometry,
    val isOpen: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(Geometry::class.java.classLoader)!!,
        parcel.readString()!!,
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeDouble(rating)
        parcel.writeString(vicinity)
        parcel.writeString(icon)
        parcel.writeParcelable(geometry, flags)
        parcel.writeString(isOpen)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Restaurant> {
        override fun createFromParcel(parcel: Parcel): Restaurant {
            return Restaurant(parcel)
        }

        override fun newArray(size: Int): Array<Restaurant?> {
            return arrayOfNulls(size)
        }
    }

    data class Geometry(
        val location: Location
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readParcelable(Location::class.java.classLoader)!!
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeParcelable(location, flags)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Geometry> {
            override fun createFromParcel(parcel: Parcel): Geometry {
                return Geometry(parcel)
            }

            override fun newArray(size: Int): Array<Geometry?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class Location(
        val lat: Double,
        val lng: Double
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readDouble(),
            parcel.readDouble()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeDouble(lat)
            parcel.writeDouble(lng)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Location> {
            override fun createFromParcel(parcel: Parcel): Location {
                return Location(parcel)
            }

            override fun newArray(size: Int): Array<Location?> {
                return arrayOfNulls(size)
            }
        }
    }
}

data class RestaurantResponse(
    val restaurants: List<Restaurant>,
    val nextPageToken: String
)
