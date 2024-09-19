package com.example.socialnetwork.model.entity

import android.os.Parcel
import android.os.Parcelable
import java.time.LocalDateTime

data class Group (
    val id: Long?,
    val name: String,
    val description: String,
    val creationDate: LocalDateTime,
    val isSuspended: Boolean,
    val suspendedReason: String?,
    val groupAdmin: List<GroupAdmin> = emptyList(),
    val post: List<Post> = emptyList(),
    val groupRequest: List<GroupRequest> = emptyList(),
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readSerializable() as LocalDateTime,
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.createTypedArrayList(GroupAdmin.CREATOR) ?: emptyList(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeSerializable(creationDate)
        parcel.writeByte(if (isSuspended) 1 else 0)
        parcel.writeString(suspendedReason)
        parcel.writeTypedList(groupAdmin)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Group> {
        override fun createFromParcel(parcel: Parcel): Group {
            return Group(parcel)
        }

        override fun newArray(size: Int): Array<Group?> {
            return arrayOfNulls(size)
        }
    }
}
data class CreateGroupRequest(
    val name: String,
    val description: String
)
