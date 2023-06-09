package com.example.github_viewer

import android.os.Parcel
import android.os.Parcelable

data class RepositoryDetails(
    val name: String,
    val description: String,
    val language: String,
    val forks: Int,
    val watchers: Int,
    val stargazers_count: Int,
    val html_url: String,
    val license: License?,
    val readme: String,
    var isFavorite: Boolean
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readParcelable(License::class.java.classLoader),
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(language)
        parcel.writeInt(forks)
        parcel.writeInt(stargazers_count)
        parcel.writeInt(watchers)
        parcel.writeString(html_url)
        parcel.writeParcelable(license, flags)
        parcel.writeString(readme)
        parcel.writeByte(if (isFavorite) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RepositoryDetails> {
        override fun createFromParcel(parcel: Parcel): RepositoryDetails {
            return RepositoryDetails(parcel)
        }

        override fun newArray(size: Int): Array<RepositoryDetails?> {
            return arrayOfNulls(size)
        }
    }
}

data class License(
    val name: String,
    val url: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<License> {
        override fun createFromParcel(parcel: Parcel): License {
            return License(parcel)
        }

        override fun newArray(size: Int): Array<License?> {
            return arrayOfNulls(size)
        }
    }
}
