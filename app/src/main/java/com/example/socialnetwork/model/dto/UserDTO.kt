package com.example.socialnetwork.model.dto

import com.example.socialnetwork.model.entity.User

data class UserDTO(
    var id: Long? = null,
    var username: String,
    var password: String,
    var email: String,
    var firstName: String,
    var lastName: String
) {
    constructor(createdUser: User) : this(
        id = createdUser.id,
        username = createdUser.username,
        password = "",
        email = createdUser.email,
        firstName = createdUser.firstName,
        lastName = createdUser.lastName
    )
}
