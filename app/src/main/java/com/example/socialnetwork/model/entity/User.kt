package com.example.socialnetwork.model.entity

import java.time.LocalDateTime
import android.os.Parcel
import android.os.Parcelable

data class User(
    val id: Long? = null,
    val username: String,
    val password: String? = null,
    val email: String,
    val lastLogin: LocalDateTime? = null,
    val firstName: String,
    val lastName: String,
    val type: EUserType = EUserType.USER,
    val description: String? = null,
    val profileName: String? = null,
    val image: Image? = null,
    val groupAdmins: List<GroupAdmin> = emptyList(),
    val post: List<Post> = emptyList(),
    val reaction: List<Reaction> = emptyList(),
    val report: List<Report> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val groupRequest: List<GroupRequest> = emptyList(),
    val sentFriendRequests: List<FriendRequest> = emptyList(),
    val receivedFriendRequests: List<FriendRequest> = emptyList()
 ): Parcelable {
    constructor(parcel: Parcel) : this(
    parcel.readValue(Long::class.java.classLoader) as? Long,
    parcel.readString() ?: "",
    parcel.readString(),
    parcel.readString() ?: "",
    parcel.readSerializable() as? LocalDateTime,
    parcel.readString() ?: "",
    parcel.readString() ?: "",
    EUserType.valueOf(parcel.readString() ?: EUserType.USER.name),
    parcel.readString(),
    parcel.readString(),
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(username)
        parcel.writeString(password)
        parcel.writeString(email)
        parcel.writeSerializable(lastLogin)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(type.name)
        parcel.writeString(description)
        parcel.writeString(profileName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}
