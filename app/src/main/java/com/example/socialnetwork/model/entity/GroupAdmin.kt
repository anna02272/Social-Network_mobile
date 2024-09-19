package com.example.socialnetwork.model.entity

import android.os.Parcel
import android.os.Parcelable


data class GroupAdmin (
    val id: Long?,
    val group: Group?,
    val user: User?,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readParcelable(Group::class.java.classLoader),
        parcel.readParcelable(User::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeParcelable(group, flags)
        parcel.writeParcelable(user, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GroupAdmin> {
        override fun createFromParcel(parcel: Parcel): GroupAdmin {
            return GroupAdmin(parcel)
        }

        override fun newArray(size: Int): Array<GroupAdmin?> {
            return arrayOfNulls(size)
        }
    }
}